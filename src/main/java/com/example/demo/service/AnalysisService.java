package com.example.demo.service;

import com.example.demo.dto.*;
import com.example.demo.entity.Detection;
import com.example.demo.repository.DetectionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisService {

    private final DetectionRepository detectionRepository;
    private final CacheService cacheService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // Prefijos para las claves de caché
    private static final String CACHE_KEY_TOTAL_VOLUME = "totalVolume";
    private static final String CACHE_KEY_VOLUME_BY_LANE = "volumeByLane";
    private static final String CACHE_KEY_HOURLY_PATTERNS = "hourlyPatterns";
    private static final String CACHE_KEY_AVG_SPEED_BY_LANE = "avgSpeedByLane";
    private static final String CACHE_KEY_BOTTLENECKS = "bottlenecks";
    private static final String CACHE_KEY_TRAFFIC_EVOLUTION = "trafficEvolution";
    private static final String CACHE_KEY_SPEED_EVOLUTION = "speedEvolution";
    private static final String CACHE_KEY_VEHICLE_TYPE_DOMINANCE = "vehicleTypeDominance";
    
    // Tiempo de caché por defecto (5 minutos)
    private static final long CACHE_DURATION = 300000;

    /**
     * Limpia la caché cada 10 minutos para asegurar datos frescos
     */
    @Scheduled(fixedRate = 600000)
    public void refreshCache() {
        log.info("Refrescando caché programada");
        cacheService.clear();
    }

    public TotalVolumeDTO getTotalVolume() {
        // Intentar obtener desde caché
        TotalVolumeDTO cachedResult = (TotalVolumeDTO) cacheService.get(CACHE_KEY_TOTAL_VOLUME);
        if (cachedResult != null) {
            log.debug("Devolviendo datos de volumen total desde caché");
            return cachedResult;
        }

        log.debug("Calculando datos de volumen total");
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
        
        TotalVolumeDTO result = TotalVolumeDTO.builder()
                .hourly(hourlyData)
                .daily(dailyData)
                .total(totalCount)
                .build();
        
        // Guardar en caché
        cacheService.put(CACHE_KEY_TOTAL_VOLUME, result, CACHE_DURATION);
        
        return result;
    }

    public Map<String, Map<String, Integer>> getVolumeByLane() {
        // Intentar obtener desde caché
        @SuppressWarnings("unchecked")
        Map<String, Map<String, Integer>> cachedResult = 
                (Map<String, Map<String, Integer>>) cacheService.get(CACHE_KEY_VOLUME_BY_LANE);
        
        if (cachedResult != null) {
            log.debug("Devolviendo datos de volumen por carril desde caché");
            return cachedResult;
        }
        
        log.debug("Calculando datos de volumen por carril");
        List<Detection> detections = detectionRepository.findAll();
        Detection latestDetection = detections.stream()
                .max(Comparator.comparing(Detection::getTimestampMs))
                .orElse(null);
        
        if (latestDetection == null) {
            log.warn("No se encontraron detecciones para obtener el volumen por carril");
            return new HashMap<>();
        }
        
        try {
            Map<String, Map<String, Integer>> result = objectMapper.readValue(
                    latestDetection.getObjectsByLane(), 
                    new TypeReference<Map<String, Map<String, Integer>>>() {});
            
            // Guardar en caché
            cacheService.put(CACHE_KEY_VOLUME_BY_LANE, result, CACHE_DURATION);
            
            return result;
        } catch (JsonProcessingException e) {
            log.error("Error al procesar objetos por carril: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    public Map<String, Integer> getHourlyPatterns() {
        // Intentar obtener desde caché
        @SuppressWarnings("unchecked")
        Map<String, Integer> cachedResult = (Map<String, Integer>) cacheService.get(CACHE_KEY_HOURLY_PATTERNS);
        
        if (cachedResult != null) {
            log.debug("Devolviendo patrones horarios desde caché");
            return cachedResult;
        }
        
        log.debug("Calculando patrones horarios");
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
        
        // Guardar en caché
        cacheService.put(CACHE_KEY_HOURLY_PATTERNS, hourlyPatterns, CACHE_DURATION);
        
        return hourlyPatterns;
    }

    public Map<String, Double> getAvgSpeedByLane() {
        // Intentar obtener desde caché
        @SuppressWarnings("unchecked")
        Map<String, Double> cachedResult = (Map<String, Double>) cacheService.get(CACHE_KEY_AVG_SPEED_BY_LANE);
        
        if (cachedResult != null) {
            log.debug("Devolviendo velocidad promedio por carril desde caché");
            return cachedResult;
        }
        
        log.debug("Calculando velocidad promedio por carril");
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
        
        // Guardar en caché
        cacheService.put(CACHE_KEY_AVG_SPEED_BY_LANE, result, CACHE_DURATION);
        
        return result;
    }

    public List<BottleneckDTO> getBottlenecks() {
        // Intentar obtener desde caché
        @SuppressWarnings("unchecked")
        List<BottleneckDTO> cachedResult = (List<BottleneckDTO>) cacheService.get(CACHE_KEY_BOTTLENECKS);
        
        if (cachedResult != null) {
            log.debug("Devolviendo cuellos de botella desde caché");
            return cachedResult;
        }
        
        log.debug("Calculando cuellos de botella");
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
        
        // Guardar en caché
        cacheService.put(CACHE_KEY_BOTTLENECKS, bottlenecks, CACHE_DURATION);
        
        return bottlenecks;
    }

    public TrafficEvolutionDTO getTrafficEvolution() {
        // Intentar obtener desde caché
        TrafficEvolutionDTO cachedResult = (TrafficEvolutionDTO) cacheService.get(CACHE_KEY_TRAFFIC_EVOLUTION);
        
        if (cachedResult != null) {
            log.debug("Devolviendo evolución de tráfico desde caché");
            return cachedResult;
        }
        
        log.debug("Calculando evolución de tráfico");
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
        
        TrafficEvolutionDTO result = TrafficEvolutionDTO.builder()
                .timestamps(timestamps)
                .car(cars)
                .bus(buses)
                .truck(trucks)
                .build();
        
        // Guardar en caché
        cacheService.put(CACHE_KEY_TRAFFIC_EVOLUTION, result, CACHE_DURATION);
        
        return result;
    }

    public SpeedEvolutionDTO getSpeedEvolution() {
        // Intentar obtener desde caché
        SpeedEvolutionDTO cachedResult = (SpeedEvolutionDTO) cacheService.get(CACHE_KEY_SPEED_EVOLUTION);
        
        if (cachedResult != null) {
            log.debug("Devolviendo evolución de velocidad desde caché");
            return cachedResult;
        }
        
        log.debug("Calculando evolución de velocidad");
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
        
        SpeedEvolutionDTO result = SpeedEvolutionDTO.builder()
                .timestamps(timestamps)
                .lane_1(lane1Speeds)
                .lane_2(lane2Speeds)
                .lane_3(lane3Speeds)
                .build();
        
        // Guardar en caché
        cacheService.put(CACHE_KEY_SPEED_EVOLUTION, result, CACHE_DURATION);
        
        return result;
    }

    public Map<String, Double> getVehicleTypeDominance() {
        // Intentar obtener desde caché
        @SuppressWarnings("unchecked")
        Map<String, Double> cachedResult = (Map<String, Double>) cacheService.get(CACHE_KEY_VEHICLE_TYPE_DOMINANCE);
        
        if (cachedResult != null) {
            log.debug("Devolviendo dominancia por tipo de vehículo desde caché");
            return cachedResult;
        }
        
        log.debug("Calculando dominancia por tipo de vehículo");
        // Calcular la distribución de tipos de vehículos
        TotalVolumeDTO totalVolume = getTotalVolume();
        Map<String, Integer> totals = totalVolume.getTotal();
        
        int sum = totals.values().stream().mapToInt(Integer::intValue).sum();
        Map<String, Double> dominance = new HashMap<>();
        
        for (Map.Entry<String, Integer> entry : totals.entrySet()) {
            double percentage = (sum > 0) ? (entry.getValue() * 100.0 / sum) : 0.0;
            dominance.put(entry.getKey(), percentage);
        }
        
        // Guardar en caché
        cacheService.put(CACHE_KEY_VEHICLE_TYPE_DOMINANCE, dominance, CACHE_DURATION);
        
        return dominance;
    }

    public List<Integer> getArrayData() {
        return Arrays.asList(45, 23, 78, 12, 90, 32, 56, 67, 89, 21);
    }

    public List<ListItemDTO> getLinkedListData() {
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
        return getLinkedListData();
    }

    public List<ListItemDTO> getCircularDoubleLinkedListData() {
        return getLinkedListData();
    }

    public List<ListItemDTO> getStackData() {
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
        return getStackData();
    }

    public TreeNodeDTO getTreeData() {
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