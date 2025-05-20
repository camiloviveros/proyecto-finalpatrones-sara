package com.example.demo.controller;

import com.example.demo.dto.*;
import com.example.demo.service.AnalysisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/detections")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DetectionController {
    
    private final AnalysisService analysisService;

    @GetMapping("/volume/total")
    public TotalVolumeDTO getTotalVolume() {
        return analysisService.getTotalVolume();
    }

    @GetMapping("/volume/by-lane")
    public Map<String, Map<String, Integer>> getVolumeByLane() {
        return analysisService.getVolumeByLane();
    }

    @GetMapping("/patterns/hourly")
    public Map<String, Integer> getHourlyPatterns() {
        return analysisService.getHourlyPatterns();
    }

    @GetMapping("/lanes/speed")
    public Map<String, Double> getAvgSpeedByLane() {
        return analysisService.getAvgSpeedByLane();
    }

    @GetMapping("/lanes/bottlenecks")
    public List<BottleneckDTO> getBottlenecks() {
        return analysisService.getBottlenecks();
    }

    @GetMapping("/temporal/evolution")
    public TrafficEvolutionDTO getTrafficEvolution() {
        return analysisService.getTrafficEvolution();
    }

    @GetMapping("/temporal/speed")
    public SpeedEvolutionDTO getSpeedEvolution() {
        return analysisService.getSpeedEvolution();
    }

    @GetMapping("/vehicle-types/dominance")
    public Map<String, Double> getVehicleTypeDominance() {
        return analysisService.getVehicleTypeDominance();
    }

    @GetMapping("/structures/array")
    public List<Integer> getArrayData() {
        return analysisService.getArrayData();
    }

    @GetMapping("/structures/linked-list")
    public List<ListItemDTO> getLinkedListData() {
        return analysisService.getLinkedListData();
    }

    @GetMapping("/structures/double-linked-list")
    public List<ListItemDTO> getDoubleLinkedListData() {
        return analysisService.getDoubleLinkedListData();
    }

    @GetMapping("/structures/circular-double-linked-list")
    public List<ListItemDTO> getCircularDoubleLinkedListData() {
        return analysisService.getCircularDoubleLinkedListData();
    }

    @GetMapping("/structures/stack")
    public List<ListItemDTO> getStackData() {
        return analysisService.getStackData();
    }

    @GetMapping("/structures/queue")
    public List<ListItemDTO> getQueueData() {
        return analysisService.getQueueData();
    }

    @GetMapping("/structures/tree")
    public TreeNodeDTO getTreeData() {
        return analysisService.getTreeData();
    }
}