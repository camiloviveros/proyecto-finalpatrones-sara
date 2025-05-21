package com.trafficanalysis.controller;

import com.trafficanalysis.dto.*;
import com.trafficanalysis.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/detections")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@Slf4j
public class DetectionController {
    
    private final AnalysisService analysisService;

    @GetMapping("/volume/total")
    public ResponseEntity<TotalVolumeDto> getTotalVolume() {
        log.info("API request: /volume/total");
        return ResponseEntity.ok(analysisService.getTotalVolume());
    }

    @GetMapping("/volume/by-lane")
    public ResponseEntity<Map<String, Map<String, Integer>>> getVolumeByLane() {
        log.info("API request: /volume/by-lane");
        return ResponseEntity.ok(analysisService.getVolumeByLane());
    }

    @GetMapping("/patterns/hourly")
    public ResponseEntity<Map<String, Integer>> getHourlyPatterns() {
        log.info("API request: /patterns/hourly");
        return ResponseEntity.ok(analysisService.getHourlyPatterns());
    }

    @GetMapping("/lanes/speed")
    public ResponseEntity<Map<String, Double>> getAvgSpeedByLane() {
        log.info("API request: /lanes/speed");
        return ResponseEntity.ok(analysisService.getAvgSpeedByLane());
    }

    @GetMapping("/lanes/bottlenecks")
    public ResponseEntity<List<BottleneckDto>> getBottlenecks() {
        log.info("API request: /lanes/bottlenecks");
        return ResponseEntity.ok(analysisService.getBottlenecks());
    }

    @GetMapping("/temporal/evolution")
    public ResponseEntity<TrafficEvolutionDto> getTrafficEvolution() {
        log.info("API request: /temporal/evolution");
        return ResponseEntity.ok(analysisService.getTrafficEvolution());
    }

    @GetMapping("/temporal/speed")
    public ResponseEntity<SpeedEvolutionDto> getSpeedEvolution() {
        log.info("API request: /temporal/speed");
        return ResponseEntity.ok(analysisService.getSpeedEvolution());
    }

    @GetMapping("/vehicle-types/dominance")
    public ResponseEntity<Map<String, Double>> getVehicleTypeDominance() {
        log.info("API request: /vehicle-types/dominance");
        return ResponseEntity.ok(analysisService.getVehicleTypeDominance());
    }

    @GetMapping("/structures/array")
    public ResponseEntity<List<Integer>> getArrayData() {
        log.info("API request: /structures/array");
        return ResponseEntity.ok(analysisService.getArrayData());
    }

    @GetMapping("/structures/linked-list")
    public ResponseEntity<List<ListItemDto>> getLinkedListData() {
        log.info("API request: /structures/linked-list");
        return ResponseEntity.ok(analysisService.getLinkedListData());
    }

    @GetMapping("/structures/double-linked-list")
    public ResponseEntity<List<ListItemDto>> getDoubleLinkedListData() {
        log.info("API request: /structures/double-linked-list");
        return ResponseEntity.ok(analysisService.getDoubleLinkedListData());
    }

    @GetMapping("/structures/circular-double-linked-list")
    public ResponseEntity<List<ListItemDto>> getCircularDoubleLinkedListData() {
        log.info("API request: /structures/circular-double-linked-list");
        return ResponseEntity.ok(analysisService.getCircularDoubleLinkedListData());
    }

    @GetMapping("/structures/stack")
    public ResponseEntity<List<ListItemDto>> getStackData() {
        log.info("API request: /structures/stack");
        return ResponseEntity.ok(analysisService.getStackData());
    }

    @GetMapping("/structures/queue")
    public ResponseEntity<List<ListItemDto>> getQueueData() {
        log.info("API request: /structures/queue");
        return ResponseEntity.ok(analysisService.getQueueData());
    }

    @GetMapping("/structures/tree")
    public ResponseEntity<TreeNodeDto> getTreeData() {
        log.info("API request: /structures/tree");
        return ResponseEntity.ok(analysisService.getTreeData());
    }
    
    @GetMapping("/analysis/summary")
    public ResponseEntity<Map<String, Object>> getAnalysisSummary() {
        log.info("API request: /analysis/summary");
        
        Map<String, Double> speeds = analysisService.getAvgSpeedByLane();
        
        double avgSpeed = speeds.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
        
        double maxSpeed = speeds.values().stream()
                .mapToDouble(Double::doubleValue)
                .max()
                .orElse(0.0);
        
        Map<String, Object> summary = Map.of(
                "averageSpeed", avgSpeed,
                "maxSpeed", maxSpeed,
                "bottleneckCount", analysisService.getBottlenecks().size()
        );
        
        return ResponseEntity.ok(summary);
    }
}