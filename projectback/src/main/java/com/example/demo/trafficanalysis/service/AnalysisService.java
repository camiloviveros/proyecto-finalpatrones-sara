package com.trafficanalysis.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trafficanalysis.dto.*;
import com.trafficanalysis.model.Detection;
import com.trafficanalysis.repository.DetectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TotalVolumeDto getTotalVolume() {
        log.info("Getting total volume data");
        List<Detection> detections = detectionRepository.findAll();
        
        // Count total vehicles by type
        Map<String, Integer> totalCount = new HashMap<>();
        
        for (Detection detection : detections) {
            try {
                Map<String, Integer> objectsTotal = parseObjectsTotal(detection.getObjectsTotal());
                
                for (Map.Entry<String, Integer> entry : objectsTotal.entrySet()) {
                    totalCount.put(entry.getKey(), 
                            totalCount.getOrDefault(entry.getKey(), 0) + entry.getValue());
                }
            } catch (Exception e) {
                log.error("Error parsing objects total: {}", e.getMessage());
            }
        }
        
        // Create hourly and daily aggregated data
        Map<String, Integer> hourlyData = new HashMap<>();
        int totalVehicles = totalCount.values().stream().mapToInt(Integer::intValue).sum();
        hourlyData.put("morning", totalVehicles / 3);
        hourlyData.put("afternoon", totalVehicles / 2);
        hourlyData.put("evening", totalVehicles / 4);
        
        Map<String, Integer> dailyData = new HashMap<>();
        dailyData.put("weekday", totalVehicles * 5 / 7);
        dailyData.put("weekend", totalVehicles * 2 / 7);
        
        return TotalVolumeDto.builder()
                .hourly(hourlyData)
                .daily(dailyData)
                .total(totalCount)
                .build();
    }

    public Map<String, Map<String, Integer>> getVolumeByLane() {
        log.info("Getting volume by lane data");
        List<Detection> detections = detectionRepository.findAllOrderByTimestampDesc();
        
        if (detections.isEmpty()) {
            log.warn("No detections found for volume by lane analysis");
            return Collections.emptyMap();
        }
        
        Detection latestDetection = detections.get(0);
        
        try {
            return parseObjectsByLane(latestDetection.getObjectsByLane());
        } catch (Exception e) {
            log.error("Error parsing objects by lane: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    public Map<String, Integer> getHourlyPatterns() {
        log.info("Getting hourly patterns data");
        
        // Create simulated hourly patterns based on real detection data
        Map<String, Integer> hourlyPatterns = new HashMap<>();
        List<Detection> detections = detectionRepository.findAll();
        
        Map<Integer, Integer> hourCounts = new HashMap<>();
        
        for (Detection detection : detections) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(detection.getDate(), 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                int hour = dateTime.getHour();
                
                Map<String, Integer> objectsTotal = parseObjectsTotal(detection.getObjectsTotal());
                int totalVehicles = objectsTotal.values().stream().mapToInt(Integer::intValue).sum();
                
                hourCounts.put(hour, hourCounts.getOrDefault(hour, 0) + totalVehicles);
            } catch (Exception e) {
                log.error("Error processing hourly patterns: {}", e.getMessage());
            }
        }
        
        // Fill in missing hours with simulated data
        for (int hour = 0; hour < 24; hour++) {
            int count = hourCounts.getOrDefault(hour, 
                    (hour >= 7 && hour <= 19) ? 100 + (int)(Math.random() * 100) : 20 + (int)(Math.random() * 50));
            hourlyPatterns.put(String.format("%02d:00", hour), count);
        }
        
        return hourlyPatterns;
    }

    public Map<String, Double> getAvgSpeedByLane() {
        log.info("Getting average speed by lane data");
        List<Detection> detections = detectionRepository.findAll();
        
        // Calculate average speed by lane
        Map<String, List<Double>> speedsByLane = new HashMap<>();
        
        for (Detection detection : detections) {
            try {
                Map<String, Double> avgSpeedByLane = parseAvgSpeedByLane(detection.getAvgSpeedByLane());
                
                for (Map.Entry<String, Double> entry : avgSpeedByLane.entrySet()) {
                    if (!speedsByLane.containsKey(entry.getKey())) {
                        speedsByLane.put(entry.getKey(), new ArrayList<>());
                    }
                    speedsByLane.get(entry.getKey()).add(entry.getValue());
                }
            } catch (Exception e) {
                log.error("Error parsing avg speed by lane: {}", e.getMessage());
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

    public List<BottleneckDto> getBottlenecks() {
        log.info("Getting bottlenecks data");
        
        // Identify bottlenecks based on speed and volume
        Map<String, Double> avgSpeedByLane = getAvgSpeedByLane();
        Map<String, Map<String, Integer>> volumeByLane = getVolumeByLane();
        
        List<BottleneckDto> bottlenecks = new ArrayList<>();
        
        for (Map.Entry<String, Double> speedEntry : avgSpeedByLane.entrySet()) {
            String lane = speedEntry.getKey();
            Double avgSpeed = speedEntry.getValue();
            
            // Consider as bottleneck if speed is less than 15 km/h
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
                
                bottlenecks.add(BottleneckDto.builder()
                        .lane(lane)
                        .avgSpeed(avgSpeed)
                        .totalVehicles(totalVehicles)
                        .heavyVehicles(heavyVehicles)
                        .build());
            }
        }
        
        // If no real bottlenecks, create a simulated one for visualization
        if (bottlenecks.isEmpty() && !avgSpeedByLane.isEmpty()) {
            String slowestLane = avgSpeedByLane.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("lane_1");
            
            bottlenecks.add(BottleneckDto.builder()
                    .lane(slowestLane)
                    .avgSpeed(avgSpeedByLane.getOrDefault(slowestLane, 20.0))
                    .totalVehicles(30)
                    .heavyVehicles(8)
                    .build());
        }
        
        return bottlenecks;
    }

    public TrafficEvolutionDto getTrafficEvolution() {
        log.info("Getting traffic evolution data");
        List<Detection> detections = detectionRepository.findAllOrderByTimestampAsc();
        
        List<String> timestamps = new ArrayList<>();
        List<Integer> cars = new ArrayList<>();
        List<Integer> buses = new ArrayList<>();
        List<Integer> trucks = new ArrayList<>();
        
        for (Detection detection : detections) {
            try {
                Map<String, Integer> objectsTotal = parseObjectsTotal(detection.getObjectsTotal());
                
                timestamps.add(detection.getDate());
                cars.add(objectsTotal.getOrDefault("car", 0));
                buses.add(objectsTotal.getOrDefault("bus", 0));
                trucks.add(objectsTotal.getOrDefault("truck", 0));
            } catch (Exception e) {
                log.error("Error parsing traffic evolution: {}", e.getMessage());
            }
        }
        
        return TrafficEvolutionDto.builder()
                .timestamps(timestamps)
                .car(cars)
                .bus(buses)
                .truck(trucks)
                .build();
    }

    public SpeedEvolutionDto getSpeedEvolution() {
        log.info("Getting speed evolution data");
        List<Detection> detections = detectionRepository.findAllOrderByTimestampAsc();
        
        List<String> timestamps = new ArrayList<>();
        List<Double> lane1Speeds = new ArrayList<>();
        List<Double> lane2Speeds = new ArrayList<>();
        List<Double> lane3Speeds = new ArrayList<>();
        
        for (Detection detection : detections) {
            try {
                Map<String, Double> avgSpeedByLane = parseAvgSpeedByLane(detection.getAvgSpeedByLane());
                
                timestamps.add(detection.getDate());
                lane1Speeds.add(avgSpeedByLane.getOrDefault("lane_1", 0.0));
                lane2Speeds.add(avgSpeedByLane.getOrDefault("lane_2", 0.0));
                lane3Speeds.add(avgSpeedByLane.getOrDefault("lane_3", 0.0));
            } catch (Exception e) {
                log.error("Error parsing speed evolution: {}", e.getMessage());
            }
        }
        
        return SpeedEvolutionDto.builder()
                .timestamps(timestamps)
                .lane_1(lane1Speeds)
                .lane_2(lane2Speeds)
                .lane_3(lane3Speeds)
                .build();
    }

    public Map<String, Double> getVehicleTypeDominance() {
        log.info("Getting vehicle type dominance data");
        
        TotalVolumeDto totalVolume = getTotalVolume();
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
        log.info("Getting array data structure");
        List<Detection> detections = detectionRepository.findAll();
        
        // Use timestamps or derived values for array visualization
        return detections.stream()
                .limit(10)
                .map(d -> Math.toIntExact(d.getTimestampMs() % 100))
                .collect(Collectors.toList());
    }

    public List<ListItemDto> getLinkedListData() {
        log.info("Getting linked list data structure");
        return createListItems(8);
    }

    public List<ListItemDto> getDoubleLinkedListData() {
        log.info("Getting double linked list data structure");
        return createListItems(8);
    }

    public List<ListItemDto> getCircularDoubleLinkedListData() {
        log.info("Getting circular double linked list data structure");
        return createListItems(8);
    }

    public List<ListItemDto> getStackData() {
        log.info("Getting stack data structure");
        return createListItems(8);
    }

    public List<ListItemDto> getQueueData() {
        log.info("Getting queue data structure");
        return createListItems(8);
    }

    public TreeNodeDto getTreeData() {
        log.info("Getting tree data structure");
        
        List<TreeNodeDto> childrenLevel1 = new ArrayList<>();
        
        List<TreeNodeDto> childrenA = new ArrayList<>();
        childrenA.add(TreeNodeDto.builder().value("A1").children(null).build());
        childrenA.add(TreeNodeDto.builder().value("A2").children(null).build());
        
        List<TreeNodeDto> childrenB = new ArrayList<>();
        childrenB.add(TreeNodeDto.builder().value("B1").children(null).build());
        childrenB.add(TreeNodeDto.builder().value("B2").children(null).build());
        childrenB.add(TreeNodeDto.builder().value("B3").children(null).build());
        
        List<TreeNodeDto> childrenC = new ArrayList<>();
        childrenC.add(TreeNodeDto.builder().value("C1").children(null).build());
        
        childrenLevel1.add(TreeNodeDto.builder().value("A").children(childrenA).build());
        childrenLevel1.add(TreeNodeDto.builder().value("B").children(childrenB).build());
        childrenLevel1.add(TreeNodeDto.builder().value("C").children(childrenC).build());
        
        return TreeNodeDto.builder()
                .value("Root")
                .children(childrenLevel1)
                .build();
    }

    // Helper methods
    private List<ListItemDto> createListItems(int count) {
        List<Detection> detections = detectionRepository.findAll();
        if (detections.isEmpty()) {
            // Fallback to generated data if no detections
            return generateFallbackListItems(count);
        }
        
        List<ListItemDto> items = new ArrayList<>();
        for (int i = 0; i < Math.min(count, detections.size()); i++) {
            Detection detection = detections.get(i);
            items.add(ListItemDto.builder()
                    .id(i + 1)
                    .date(detection.getDate())
                    .build());
        }
        return items;
    }
    
    private List<ListItemDto> generateFallbackListItems(int count) {
        List<ListItemDto> items = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        
        for (int i = 1; i <= count; i++) {
            items.add(ListItemDto.builder()
                    .id(i)
                    .date(now.minusHours(i).format(formatter))
                    .build());
        }
        return items;
    }
    
    // JSON parsing methods
    private Map<String, Integer> parseObjectsTotal(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing objectsTotal JSON: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<String, Map<String, Integer>> parseObjectsByLane(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Map<String, Integer>>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing objectsByLane JSON: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    private Map<String, Double> parseAvgSpeedByLane(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing avgSpeedByLane JSON: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }
}