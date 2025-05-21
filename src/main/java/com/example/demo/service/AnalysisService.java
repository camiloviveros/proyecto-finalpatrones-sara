package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.Detection;
import com.example.demo.repository.DetectionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j // Agregamos SLF4J para logging
public class AnalysisService {

    private final DetectionRepository detectionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TotalVolumeDTO getTotalVolume() {
        List<Detection> detections = detectionRepository.findAll();
        
        // Contar total de vehículos por tipo
        Map<String, Integer> totalCount = new HashMap<>();
        
        for (Detection detection : detections) {
            try {
                Map<String, Integer> objectsTotal = objectMapper.readValue(
                        detection.getObjectsTotal(), 
                        new TypeReference<Map<String, Integer>>() {});
                
                for (Map.Entry<String, Integer> entry : objectsTotal.entrySet()) {
                    totalCount.put(entry.getKey(), 
                            totalCount.getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            } catch (JsonProcessingException e) {
                log.error("Error al procesar objetos totales: {}", e.getMessage(), e);
            }
        }
        
        // Simulamos datos horarios y diarios
        Map<String, Integer> hourlyData = new HashMap<>();
        hourlyData.put("morning", totalCount.values().stream().mapToInt(Integer::intValue).sum() / 3);
        hourlyData.put("afternoon", totalCount.values().stream().mapToInt(Integer::intValue).sum() / 2);
        hourlyData.put("evening", totalCount.values().stream().mapToInt(Integer::intValue).sum() / 4);
        
        Map<String, Integer> dailyData = new HashMap<>();
        dailyData.put("weekday", totalCount.values().stream().mapToInt(Integer::intValue).sum() * 5 / 7);
        dailyData.put("weekend", totalCount.values().stream().mapToInt(Integer::intValue).sum() * 2 / 7);
        
        return TotalVolumeDTO.builder()
                .hourly(hourlyData)
                .daily(dailyData)
                .total(totalCount)
                .build();
    }

    public Map<String, Map<String, Integer>> getVolumeByLane() {
        List<Detection> detections = detectionRepository.findAll();
        Detection latestDetection = detections.stream()
                .max(Comparator.comparing(Detection::getTimestampMs))
                .orElse(null);
        
        if (latestDetection == null) {
            log.warn("No se encontraron detecciones para obtener el volumen por carril");
            return new HashMap<>();
        }
        
        try {
            return objectMapper.readValue(
                    latestDetection.getObjectsByLane(), 
                    new TypeReference<Map<String, Map<String, Integer>>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error al procesar objetos por carril: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    public Map<String, Integer> getHourlyPatterns() {
        // Simulamos patrones horarios
        Map<String, Integer> hourlyPatterns = new HashMap<>();
        hourlyPatterns.put("00:00", 20);
        hourlyPatterns.put("01:00", 15);
        hourlyPatterns.put("02:00", 10);
        hourlyPatterns.put("03:00", 8);
        hourlyPatterns.put("04:00", 12);
        hourlyPatterns.put("05:00", 25);
        hourlyPatterns.put("06:00", 60);
        hourlyPatterns.put("07:00", 120);
        hourlyPatterns.put("08:00", 180);
        hourlyPatterns.put("09:00", 150);
        hourlyPatterns.put("10:00", 130);
        hourlyPatterns.put("11:00", 140);
        hourlyPatterns.put("12:00", 160);
        hourlyPatterns.put("13:00", 170);
        hourlyPatterns.put("14:00", 150);
        hourlyPatterns.put("15:00", 145);
        hourlyPatterns.put("16:00", 160);
        hourlyPatterns.put("17:00", 190);
        hourlyPatterns.put("18:00", 210);
        hourlyPatterns.put("19:00", 180);
        hourlyPatterns.put("20:00", 120);
        hourlyPatterns.put("21:00", 90);
        hourlyPatterns.put("22:00", 60);
        hourlyPatterns.put("23:00", 40);
        
        return hourlyPatterns;
    }

    public Map<String, Double> getAvgSpeedByLane() {
        List<Detection> detections = detectionRepository.findAll();
        
        // Calcular promedio de velocidad por carril
        Map<String, List<Double>> speedsByLane = new HashMap<>();
        
        for (Detection detection : detections) {
            try {
                Map<String, Double> avgSpeedByLane = objectMapper.readValue(
                        detection.getAvgSpeedByLane(), 
                        new TypeReference<Map<String, Double>>() {});
                
                for (Map.Entry<String, Double> entry : avgSpeedByLane.entrySet()) {
                    if (!speedsByLane.containsKey(entry.getKey())) {
                        speedsByLane.put(entry.getKey(), new ArrayList<>());
                    }
                    speedsByLane.get(entry.getKey()).add(entry.getValue());
                }
            } catch (JsonProcessingException e) {
                log.error("Error al procesar velocidades por carril: {}", e.getMessage(), e);
            }
        }
        
        Map<String, Double> result = new HashMap<>();
        for (Map.Entry<String, List<Double>> entry : speedsByLane.entrySet()) {
            double avg = entry.getValue().stream()
                    .mapToDouble(Double::doubleValue)
                    .average()
                    .orElse(0.0);
            result.put(entry.getKey(), avg);
        }
        
        return result;
    }

    public List<BottleneckDTO> getBottlenecks() {
        // Identificar cuellos de botella basados en velocidad y volumen
        Map<String, Double> avgSpeedByLane = getAvgSpeedByLane();
        Map<String, Map<String, Integer>> volumeByLane = getVolumeByLane();
        
        List<BottleneckDTO> bottlenecks = new ArrayList<>();
        
        for (Map.Entry<String, Double> speedEntry : avgSpeedByLane.entrySet()) {
            String lane = speedEntry.getKey();
            Double avgSpeed = speedEntry.getValue();
            
            // Consideramos como cuello de botella si la velocidad es menor a 15 km/h
            if (avgSpeed < 15.0) {
                int totalVehicles = 0;
                int heavyVehicles = 0;
                
                if (volumeByLane.containsKey(lane)) {
                    Map<String, Integer> vehicles = volumeByLane.get(lane);
                    for (Map.Entry<String, Integer> vehicleEntry : vehicles.entrySet()) {
                        totalVehicles += vehicleEntry.getValue();
                        if (vehicleEntry.getKey().equals("truck") || vehicleEntry.getKey().equals("bus")) {
                            heavyVehicles += vehicleEntry.getValue();
                        }
                    }
                }
                
                bottlenecks.add(BottleneckDTO.builder()
                        .lane(lane)
                        .avgSpeed(avgSpeed)
                        .totalVehicles(totalVehicles)
                        .heavyVehicles(heavyVehicles)
                        .build());
            }
        }
        
        // Si no hay cuellos de botella reales, generamos uno simulado para visualización
        if (bottlenecks.isEmpty() && !avgSpeedByLane.isEmpty()) {
            String slowestLane = avgSpeedByLane.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("lane_1");
            
            bottlenecks.add(BottleneckDTO.builder()
                    .lane(slowestLane)
                    .avgSpeed(avgSpeedByLane.getOrDefault(slowestLane, 20.0))
                    .totalVehicles(30)
                    .heavyVehicles(8)
                    .build());
        }
        
        return bottlenecks;
    }

    public TrafficEvolutionDTO getTrafficEvolution() {
        List<Detection> detections = detectionRepository.findAll().stream()
                .sorted(Comparator.comparing(Detection::getTimestampMs))
                .collect(Collectors.toList());
        
        List<String> timestamps = new ArrayList<>();
        List<Integer> cars = new ArrayList<>();
        List<Integer> buses = new ArrayList<>();
        List<Integer> trucks = new ArrayList<>();
        
        for (Detection detection : detections) {
            try {
                Map<String, Integer> objectsTotal = objectMapper.readValue(
                        detection.getObjectsTotal(), 
                        new TypeReference<Map<String, Integer>>() {});
                
                timestamps.add(detection.getDate());
                cars.add(objectsTotal.getOrDefault("car", 0));
                buses.add(objectsTotal.getOrDefault("bus", 0));
                trucks.add(objectsTotal.getOrDefault("truck", 0));
            } catch (JsonProcessingException e) {
                log.error("Error al procesar evolución del tráfico: {}", e.getMessage(), e);
            }
        }
        
        return TrafficEvolutionDTO.builder()
                .timestamps(timestamps)
                .car(cars)
                .bus(buses)
                .truck(trucks)
                .build();
    }

    public SpeedEvolutionDTO getSpeedEvolution() {
        List<Detection> detections = detectionRepository.findAll().stream()
                .sorted(Comparator.comparing(Detection::getTimestampMs))
                .collect(Collectors.toList());
        
        List<String> timestamps = new ArrayList<>();
        List<Double> lane1Speeds = new ArrayList<>();
        List<Double> lane2Speeds = new ArrayList<>();
        List<Double> lane3Speeds = new ArrayList<>();
        
        for (Detection detection : detections) {
            try {
                Map<String, Double> avgSpeedByLane = objectMapper.readValue(
                        detection.getAvgSpeedByLane(), 
                        new TypeReference<Map<String, Double>>() {});
                
                timestamps.add(detection.getDate());
                lane1Speeds.add(avgSpeedByLane.getOrDefault("lane_1", 0.0));
                lane2Speeds.add(avgSpeedByLane.getOrDefault("lane_2", 0.0));
                lane3Speeds.add(avgSpeedByLane.getOrDefault("lane_3", 0.0));
            } catch (JsonProcessingException e) {
                log.error("Error al procesar evolución de velocidad: {}", e.getMessage(), e);
            }
        }
        
        return SpeedEvolutionDTO.builder()
                .timestamps(timestamps)
                .lane_1(lane1Speeds)
                .lane_2(lane2Speeds)
                .lane_3(lane3Speeds)
                .build();
    }

    public Map<String, Double> getVehicleTypeDominance() {
        // Calcular la distribución de tipos de vehículos
        TotalVolumeDTO totalVolume = getTotalVolume();
        Map<String, Integer> totals = totalVolume.getTotal();
        
        int sum = totals.values().stream().mapToInt(Integer::intValue).sum();
        Map<String, Double> dominance = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : totals.entrySet()) {
            double percentage = (sum > 0) ? (entry.getValue() * 100.0 / sum) : 0.0;
            dominance.put(entry.getKey(), percentage);
        }
        
        return dominance;
    }

    public List<Integer> getArrayData() {
        // Datos de ejemplo para visualización de arrays
        return Arrays.asList(45, 23, 78, 12, 90, 32, 56, 67, 89, 21);
    }

    public List<ListItemDTO> getLinkedListData() {
        // Datos de ejemplo para visualización de listas enlazadas
        List<ListItemDTO> linkedList = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 1; i <= 8; i++) {
            linkedList.add(ListItemDTO.builder()
                    .id(i)
                    .date(now.minusHours(i).format(formatter))
                    .build());
        }
        
        return linkedList;
    }

    public List<ListItemDTO> getDoubleLinkedListData() {
        // Reutilizamos los datos de la lista enlazada simple
        return getLinkedListData();
    }

    public List<ListItemDTO> getCircularDoubleLinkedListData() {
        // Reutilizamos los datos de la lista enlazada simple
        return getLinkedListData();
    }

    public List<ListItemDTO> getStackData() {
        // Datos de ejemplo para visualización de pilas
        List<ListItemDTO> stack = new ArrayList<>();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 1; i <= 8; i++) {
            stack.add(ListItemDTO.builder()
                    .id(i)
                    .date(now.minusMinutes(i * 5).format(formatter))
                    .build());
        }
        
        return stack;
    }

    public List<ListItemDTO> getQueueData() {
        // Datos de ejemplo para visualización de colas
        return getStackData();
    }

    public TreeNodeDTO getTreeData() {
        // Datos de ejemplo para visualización de árboles
        List<TreeNodeDTO> childrenLevel1 = new ArrayList<>();
        
        List<TreeNodeDTO> childrenA = new ArrayList<>();
        childrenA.add(TreeNodeDTO.builder().value("A1").children(null).build());
        childrenA.add(TreeNodeDTO.builder().value("A2").children(null).build());
        
        List<TreeNodeDTO> childrenB = new ArrayList<>();
        childrenB.add(TreeNodeDTO.builder().value("B1").children(null).build());
        childrenB.add(TreeNodeDTO.builder().value("B2").children(null).build());
        childrenB.add(TreeNodeDTO.builder().value("B3").children(null).build());
        
        List<TreeNodeDTO> childrenC = new ArrayList<>();
        childrenC.add(TreeNodeDTO.builder().value("C1").children(null).build());
        
        childrenLevel1.add(TreeNodeDTO.builder().value("A").children(childrenA).build());
        childrenLevel1.add(TreeNodeDTO.builder().value("B").children(childrenB).build());
        childrenLevel1.add(TreeNodeDTO.builder().value("C").children(childrenC).build());
        
        return TreeNodeDTO.builder()
                .value("Root")
                .children(childrenLevel1)
                .build();
    }
}