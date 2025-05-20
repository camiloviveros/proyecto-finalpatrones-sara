import cv2
import json
import time
import numpy as np
from collections import defaultdict
from ultralytics import YOLO
from deep_sort_realtime.deepsort_tracker import DeepSort
from sklearn.cluster import DBSCAN
from datetime import datetime  


model = YOLO("yolov8n.pt")
cap = cv2.VideoCapture("data/video1.mp4")
tracker = DeepSort(max_age=30)

if not cap.isOpened():
    print("Error al abrir el video")
    exit()

fps = cap.get(cv2.CAP_PROP_FPS)
frame_delay = int(1000 / fps)
original_size = (int(cap.get(cv2.CAP_PROP_FRAME_WIDTH)), int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT)))
display_size = (700, int(700 * original_size[1] / original_size[0]))

cv2.namedWindow("Detección ", cv2.WINDOW_NORMAL)
cv2.resizeWindow("Detección ", display_size[0], display_size[1])

resumen_data = {"detections": []}
last_annotated = None
detection_interval = 700  
last_detection_time = 0
start_time = time.time() * 1000  

eps_dbscan = 50  
min_samples_dbscan = 1  


def detectar_carriles(frame):
    gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(gray, (5, 5), 0)
    edges = cv2.Canny(blur, 50, 150, apertureSize=3)

    lines = cv2.HoughLinesP(edges, 1, np.pi / 180, threshold=100, minLineLength=100, maxLineGap=50)
    if lines is None:
        return []

    vertical_lines = []
    for line in lines:
        x1, y1, x2, y2 = line[0]
        if abs(x2 - x1) > 50: 
            x_center = int((x1 + x2) / 2)
            vertical_lines.append([x_center])

    if len(vertical_lines) < 2:
        return []  

    # Convertir a numpy array para que DBSCAN lo acepte correctamente
    vertical_lines_array = np.array(vertical_lines)
    clustering = DBSCAN(eps=eps_dbscan, min_samples=min_samples_dbscan).fit(vertical_lines_array)
    labels = clustering.labels_

    carril_lines = []
    unique_labels = set(labels)
    for label in unique_labels:
        if label == -1:
            continue  
        cluster_points = [vertical_lines[i][0] for i in range(len(vertical_lines)) if labels[i] == label]
        x_mean = int(np.mean(cluster_points))
        carril_lines.append(x_mean)

    carril_lines = sorted(carril_lines)

    carriles = []
    for i in range(len(carril_lines) - 1):
        carriles.append((carril_lines[i], carril_lines[i + 1]))

    carriles.insert(0, (0, carril_lines[0]))
    carriles.append((carril_lines[-1], frame.shape[1]))

    return carriles


ret, frame = cap.read()
if not ret:
    print("Error leyendo primer frame")
    exit()

carriles_detectados = detectar_carriles(frame)
if len(carriles_detectados) == 0:
    print("No se detectaron carriles. Usando un solo carril por defecto.")
    carriles_detectados = [(0, frame.shape[1])]

print(f"Carriles detectados: {len(carriles_detectados)}")

cap.set(cv2.CAP_PROP_POS_FRAMES, 0)  

track_history = {}

while True:
    ret, frame = cap.read()
    if not ret:
        break

    current_time = time.time() * 1000 - start_time
    if current_time - last_detection_time >= detection_interval:
        results = model(frame)
        detections = []

        for box in results[0].boxes:
            if float(box.conf) >= 0.3:
                cls = model.names[int(box.cls)]
                if cls not in ['car', 'bus', 'truck']:
                    continue
                x1, y1, x2, y2 = map(int, box.xyxy[0])
                detections.append(([x1, y1, x2 - x1, y2 - y1], float(box.conf), cls))

        tracks = tracker.update_tracks(detections, frame=frame)

        counts_total = defaultdict(int)
        counts_by_lane = defaultdict(lambda: defaultdict(int))
        speeds_by_lane = defaultdict(list)

        for track in tracks:
            if not track.is_confirmed():
                continue
            track_id = track.track_id
            ltrb = track.to_ltrb()
            x1, y1, x2, y2 = map(int, ltrb)
            x_center = int((x1 + x2) / 2)
            y_center = int((y1 + y2) / 2)
            cls = track.get_det_class()

            lane_id = None
            for idx, (xmin, xmax) in enumerate(carriles_detectados):
                if xmin <= x_center < xmax:
                    lane_id = f"lane_{idx+1}"
                    break

            if track_id in track_history:
                prev_x, prev_y, prev_time = track_history[track_id]
                dx = x_center - prev_x
                dy = y_center - prev_y
                distance_pixels = np.sqrt(dx**2 + dy**2)
                time_diff = (current_time - prev_time) / 1000  
                if time_diff > 0:
                    speed = (distance_pixels / time_diff) * 3.6  
                    if lane_id:
                        speeds_by_lane[lane_id].append(speed)
            track_history[track_id] = (x_center, y_center, current_time)

            if lane_id:
                counts_by_lane[lane_id][cls] += 1
            counts_total[cls] += 1
          
            cv2.rectangle(frame, (x1, y1), (x2, y2), (0, 255, 0), 2)
            label = f"{cls} ID:{track_id}"
            cv2.putText(frame, label, (x1, y1 - 10), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0, 255, 0), 2)

        for (xmin, xmax) in carriles_detectados:
            cv2.line(frame, (xmin, 0), (xmin, frame.shape[0]), (255, 0, 0), 2)

        avg_speed_by_lane = {}
        for lane, speeds in speeds_by_lane.items():
            if speeds:
                avg_speed_by_lane[lane] = sum(speeds) / len(speeds)

        fecha_actual = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

        resumen_data["detections"].append({
            "timestamp_ms": int(current_time),
            "date": fecha_actual,
            "objects_total": dict(counts_total),
            "objects_by_lane": {lane: dict(obj) for lane, obj in counts_by_lane.items()},
            "avg_speed_by_lane": avg_speed_by_lane
        })

        last_annotated = frame
        last_detection_time = current_time

    display_frame = last_annotated if last_annotated is not None else frame
    resized_frame = cv2.resize(display_frame, display_size)
    cv2.imshow("Detección Adaptativa", resized_frame)

    key = cv2.waitKey(frame_delay) & 0xFF
    if key in (ord('q'), 27): 
        break

cap.release()
cv2.destroyAllWindows()

try:
    with open("detections.json", "w") as f:
        json.dump(resumen_data, f, indent=4)
    print("Datos guardados correctamente en detections.json")
except Exception as e:
    print(f"Error guardando el archivo JSON: {e}")
