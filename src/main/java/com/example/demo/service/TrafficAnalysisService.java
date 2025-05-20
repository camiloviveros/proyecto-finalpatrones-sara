// projectback/src/main/java/com/example/demo/service/TrafficAnalysisService.java
package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Detection;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrafficAnalysisService {

    private final DetectionService detectionService;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 1. Análisis de tráfico general
    public Map<String, Map<String, Integer>> getTotalVehicleVolumeByType() {
        List<Detection> detections = detectionService.getAllDetections();
        Map<String, Integer> hourlyVolume = new HashMap<>();
        Map<String, Integer> dailyVolume = new HashMap<>();
        Map<String, Integer> totalVolume = new HashMap<>();

        detections.forEach(detection -> {
            Map<String, Integer> objectsTotal = detectionService.parseObjectsTotal(detection.getObjectsTotal());
            
            // Acumular totales
            objectsTotal.forEach((vehicleType, count) -> {
                totalVolume.merge(vehicleType, count, Integer::sum);
                
                // Extrae la hora y fecha
                LocalDateTime dateTime = LocalDateTime.parse(detection.getDate(), FORMATTER);
                String hour = dateTime.getHour() + ":00";
                String day = dateTime.toLocalDate().toString();
                
                // Acumular por hora
                hourlyVolume.merge(vehicleType + "_" + hour, count, Integer::sum);
                
                // Acumular por día
                dailyVolume.merge(vehicleType + "_" + day, count, Integer::sum);
            });
        });

        return Map.of(
            "hourly", hourlyVolume, 
            "daily", dailyVolume, 
            "total", totalVolume
        );
    }

    public Map<String, Map<String, Integer>> getVehicleVolumeByLane() {
        List<Detection> detections = detectionService.getAllDetections();
        Map<String, Map<String, Integer>> laneVolumes = new HashMap<>();

        for (Detection detection : detections) {
            Map<String, Map<String, Integer>> objectsByLane = detectionService.parseObjectsByLane(detection.getObjectsByLane());
            
            objectsByLane.forEach((lane, vehicleTypes) -> {
                if (!laneVolumes.containsKey(lane)) {
                    laneVolumes.put(lane, new HashMap<>());
                }
                
                vehicleTypes.forEach((vehicleType, count) -> 
                    laneVolumes.get(lane).merge(vehicleType, count, Integer::sum)
                );
            });
        }

        return laneVolumes;
    }

    public Map<String, Integer> getTrafficPatternsByHour() {
        List<Detection> detections = detectionService.getAllDetections();
        Map<String, Integer> hourlyCount = new HashMap<>();

        for (Detection detection : detections) {
            Map<String, Integer> objectsTotal = detectionService.parseObjectsTotal(detection.getObjectsTotal());
            LocalDateTime dateTime = LocalDateTime.parse(detection.getDate(), FORMATTER);
            String hour = dateTime.getHour() + ":00";
            
            // Suma todos los vehículos para esta hora
            int totalVehicles = objectsTotal.values().stream().mapToInt(Integer::intValue).sum();
            hourlyCount.merge(hour, totalVehicles, Integer::sum);
        }

        return hourlyCount;
    }

    // 2. Análisis de comportamiento por carril
    public Map<String, Double> getAverageSpeedByLane() {
        List<Detection> detections = detectionService.getAllDetections();
        Map<String, List<Double>> speedsByLane = new HashMap<>();

        for (Detection detection : detections) {
            Map<String, Double> avgSpeedByLane = detectionService.parseAvgSpeedByLane(detection.getAvgSpeedByLane());
            
            avgSpeedByLane.forEach((lane, speed) -> {
                if (!speedsByLane.containsKey(lane)) {
                    speedsByLane.put(lane, new ArrayList<>());
                }
                speedsByLane.get(lane).add(speed);
            });
        }

        // Calcular promedio por carril
        Map<String, Double> avgSpeedByLane = new HashMap<>();
        speedsByLane.forEach((lane, speeds) -> {
            double avgSpeed = speeds.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            avgSpeedByLane.put(lane, avgSpeed);
        });

        return avgSpeedByLane;
    }

    public List<Map<String, Object>> identifyBottlenecks() {
        // Se eliminó la variable no utilizada "detections"
        List<Map<String, Object>> bottlenecks = new ArrayList<>();
        
        // Obtener velocidad promedio por carril
        Map<String, Double> avgSpeedByLane = getAverageSpeedByLane();
        
        // Obtener volumen por carril
        Map<String, Map<String, Integer>> volumeByLane = getVehicleVolumeByLane();
        
        // Identificar cuellos de botella (carriles con alta densidad de vehículos y baja velocidad)
        avgSpeedByLane.forEach((lane, avgSpeed) -> {
            Map<String, Integer> laneVolume = volumeByLane.getOrDefault(lane, Collections.emptyMap());
            int totalVehicles = laneVolume.values().stream().mapToInt(Integer::intValue).sum();
            int heavyVehicles = laneVolume.getOrDefault("truck", 0) + laneVolume.getOrDefault("bus", 0);
            
            // Si la velocidad es baja y hay muchos vehículos, especialmente pesados
            if (avgSpeed < 10.0 && totalVehicles > 10 && heavyVehicles >= 2) {
                Map<String, Object> bottleneck = new HashMap<>();
                bottleneck.put("lane", lane);
                bottleneck.put("avgSpeed", avgSpeed);
                bottleneck.put("totalVehicles", totalVehicles);
                bottleneck.put("heavyVehicles", heavyVehicles);
                bottlenecks.add(bottleneck);
            }
        });
        
        return bottlenecks;
    }

    // 3. Análisis temporal
    public Map<String, List<Integer>> getTrafficEvolutionOverTime() {
        List<Detection> detections = detectionService.getAllDetections().stream()
                .sorted(Comparator.comparing(Detection::getTimestampMs))
                .collect(Collectors.toList());
        
        Map<String, List<Integer>> evolution = new HashMap<>();
        evolution.put("timestamps", new ArrayList<>());
        evolution.put("car", new ArrayList<>());
        evolution.put("bus", new ArrayList<>());
        evolution.put("truck", new ArrayList<>());
        
        for (Detection detection : detections) {
            Map<String, Integer> objectsTotal = detectionService.parseObjectsTotal(detection.getObjectsTotal());
            
            evolution.get("timestamps").add(detection.getTimestampMs().intValue());
            evolution.get("car").add(objectsTotal.getOrDefault("car", 0));
            evolution.get("bus").add(objectsTotal.getOrDefault("bus", 0));
            evolution.get("truck").add(objectsTotal.getOrDefault("truck", 0));
        }
        
        return evolution;
    }

    public Map<String, List<Double>> getSpeedEvolutionOverTime() {
        List<Detection> detections = detectionService.getAllDetections().stream()
                .sorted(Comparator.comparing(Detection::getTimestampMs))
                .collect(Collectors.toList());
        
        Map<String, List<Double>> evolution = new HashMap<>();
        evolution.put("timestamps", new ArrayList<>());
        evolution.put("lane_1", new ArrayList<>());
        evolution.put("lane_2", new ArrayList<>());
        evolution.put("lane_3", new ArrayList<>());
        
        for (Detection detection : detections) {
            Map<String, Double> avgSpeedByLane = detectionService.parseAvgSpeedByLane(detection.getAvgSpeedByLane());
            
            evolution.get("timestamps").add((double) detection.getTimestampMs());
            evolution.get("lane_1").add(avgSpeedByLane.getOrDefault("lane_1", 0.0));
            evolution.get("lane_2").add(avgSpeedByLane.getOrDefault("lane_2", 0.0));
            evolution.get("lane_3").add(avgSpeedByLane.getOrDefault("lane_3", 0.0));
        }
        
        return evolution;
    }

    // 4. Análisis por tipo de vehículo
    public Map<String, Double> getVehicleTypeDominance() {
        List<Detection> detections = detectionService.getAllDetections();
        Map<String, Integer> totalByType = new HashMap<>();
        
        // Calcular totales por tipo de vehículo
        for (Detection detection : detections) {
            Map<String, Integer> objectsTotal = detectionService.parseObjectsTotal(detection.getObjectsTotal());
            
            for (Map.Entry<String, Integer> entry : objectsTotal.entrySet()) {
                totalByType.merge(entry.getKey(), entry.getValue(), Integer::sum);
            }
        }
        
        // Calcular el total general (una sola vez, fuera del loop)
        final int grandTotal = totalByType.values().stream()
                                     .mapToInt(Integer::intValue)
                                     .sum();
        
        // Calcular porcentajes
        Map<String, Double> dominance = new HashMap<>();
        if (grandTotal > 0) {
            totalByType.forEach((type, count) -> 
                dominance.put(type, (count * 100.0) / grandTotal)
            );
        }
        
        return dominance;
    }
}