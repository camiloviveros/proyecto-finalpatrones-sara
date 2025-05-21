package com.example.demo.service;

import com.example.demo.entity.Detection;
import com.example.demo.repository.DetectionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class DetectionService {

    private static final Logger log = LoggerFactory.getLogger(DetectionService.class);
    
    private final DetectionRepository detectionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public DetectionService(DetectionRepository detectionRepository) {
        this.detectionRepository = detectionRepository;
    }

    @Transactional
    public void saveDetections(List<Detection> detections) {
        detectionRepository.saveAll(detections);
    }

    public List<Detection> getAllDetections() {
        return detectionRepository.findAll();
    }

    public Map<String, Integer> parseObjectsTotal(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error al analizar objectsTotal JSON: {}", e.getMessage(), e);
            return Map.of();
        }
    }

    public Map<String, Map<String, Integer>> parseObjectsByLane(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Map<String, Integer>>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error al analizar objectsByLane JSON: {}", e.getMessage(), e);
            return Map.of();
        }
    }

    public Map<String, Double> parseAvgSpeedByLane(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<Map<String, Double>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error al analizar avgSpeedByLane JSON: {}", e.getMessage(), e);
            return Map.of();
        }
    }
}