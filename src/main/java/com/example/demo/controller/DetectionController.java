// projectback/src/main/java/com/example/demo/controller/DetectionController.java
package com.example.demo.controller;

import com.example.demo.entity.Detection;
import com.example.demo.service.*;
import com.example.demo.service.datastructures.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/detections")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class DetectionController {
    private final DetectionService detectionService;
    private final TrafficAnalysisService trafficAnalysisService;
    private final DataStructureService dataStructureService;

    @GetMapping
    public ResponseEntity<List<Detection>> getAllDetections() {
        return ResponseEntity.ok(detectionService.getAllDetections());
    }

    // Análisis de tráfico general
    @GetMapping("/volume/total")
    public ResponseEntity<Map<String, Map<String, Integer>>> getTotalVehicleVolume() {
        return ResponseEntity.ok(trafficAnalysisService.getTotalVehicleVolumeByType());
    }

    @GetMapping("/volume/by-lane")
    public ResponseEntity<Map<String, Map<String, Integer>>> getVehicleVolumeByLane() {
        return ResponseEntity.ok(trafficAnalysisService.getVehicleVolumeByLane());
    }

    @GetMapping("/patterns/hourly")
    public ResponseEntity<Map<String, Integer>> getHourlyPatterns() {
        return ResponseEntity.ok(trafficAnalysisService.getTrafficPatternsByHour());
    }

    // Análisis de comportamiento por carril
    @GetMapping("/lanes/speed")
    public ResponseEntity<Map<String, Double>> getAvgSpeedByLane() {
        return ResponseEntity.ok(trafficAnalysisService.getAverageSpeedByLane());
    }

    @GetMapping("/lanes/bottlenecks")
    public ResponseEntity<List<Map<String, Object>>> getBottlenecks() {
        return ResponseEntity.ok(trafficAnalysisService.identifyBottlenecks());
    }

    // Análisis temporal
    @GetMapping("/temporal/evolution")
    public ResponseEntity<Map<String, List<Integer>>> getTrafficEvolution() {
        return ResponseEntity.ok(trafficAnalysisService.getTrafficEvolutionOverTime());
    }

    @GetMapping("/temporal/speed")
    public ResponseEntity<Map<String, List<Double>>> getSpeedEvolution() {
        return ResponseEntity.ok(trafficAnalysisService.getSpeedEvolutionOverTime());
    }

    // Análisis por tipo de vehículo
    @GetMapping("/vehicle-types/dominance")
    public ResponseEntity<Map<String, Double>> getVehicleTypeDominance() {
        return ResponseEntity.ok(trafficAnalysisService.getVehicleTypeDominance());
    }

    // Estructuras de datos
    @GetMapping("/structures/array")
    public ResponseEntity<int[]> getArrayExample() {
        return ResponseEntity.ok(dataStructureService.getDetectionsAsArray());
    }

    @GetMapping("/structures/linked-list")
    public ResponseEntity<List<Detection>> getLinkedListExample() {
        return ResponseEntity.ok(dataStructureService.getDetectionsAsLinkedList());
    }

    @GetMapping("/structures/double-linked-list")
    public ResponseEntity<List<Detection>> getDoubleLinkedListExample() {
        return ResponseEntity.ok(dataStructureService.getDetectionsAsDoubleLinkedList());
    }

    @GetMapping("/structures/circular-double-linked-list")
    public ResponseEntity<List<Detection>> getCircularDoubleLinkedListExample() {
        return ResponseEntity.ok(dataStructureService.getDetectionsAsCircularDoubleLinkedList());
    }

    @GetMapping("/structures/stack")
    public ResponseEntity<List<Detection>> getStackExample() {
        return ResponseEntity.ok(dataStructureService.getDetectionsAsStack());
    }

    @GetMapping("/structures/queue")
    public ResponseEntity<List<Detection>> getQueueExample() {
        return ResponseEntity.ok(dataStructureService.getDetectionsAsQueue());
    }

    @GetMapping("/structures/tree")
    public ResponseEntity<Map<String, Object>> getTreeExample() {
        return ResponseEntity.ok(dataStructureService.getDetectionsAsTree());
    }
}