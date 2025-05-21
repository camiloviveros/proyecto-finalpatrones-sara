package com.trafficanalysis.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trafficanalysis.dto.DetectionDto;
import com.trafficanalysis.dto.DetectionsWrapper;
import com.trafficanalysis.model.Detection;
import com.trafficanalysis.repository.DetectionRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JsonLoaderService {

    private final DetectionRepository detectionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private static final String DETECTION_FILE_PATH = "../detections/detections.json";
    
    @PostConstruct
    public void init() {
        try {
            loadJsonDataOnStartup();
            startFileWatcher();
        } catch (Exception e) {
            log.error("Error initializing JsonLoaderService", e);
        }
    }
    
    @Transactional
    public void loadJsonDataOnStartup() {
        try {
            File jsonFile = new File(DETECTION_FILE_PATH);
            if (jsonFile.exists()) {
                log.info("Loading initial data from JSON file: {}", DETECTION_FILE_PATH);
                loadAndSaveDetections(jsonFile);
            } else {
                log.warn("JSON file does not exist at path: {}", DETECTION_FILE_PATH);
            }
        } catch (Exception e) {
            log.error("Error loading initial JSON data", e);
        }
    }
    
    private void startFileWatcher() {
        new Thread(() -> {
            try {
                log.info("Starting file watcher for: {}", DETECTION_FILE_PATH);
                Path path = Paths.get(DETECTION_FILE_PATH).getParent();
                WatchService watchService = FileSystems.getDefault().newWatchService();
                path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
                
                while (true) {
                    WatchKey key = watchService.take();
                    for (WatchEvent<?> event : key.pollEvents()) {
                        Path changed = (Path) event.context();
                        if (changed.getFileName().toString().equals("detections.json")) {
                            log.info("Detected change in detections.json file");
                            File jsonFile = new File(DETECTION_FILE_PATH);
                            loadAndSaveDetections(jsonFile);
                        }
                    }
                    key.reset();
                }
            } catch (Exception e) {
                log.error("Error in file watcher", e);
            }
        }).start();
    }
    
    @Transactional
    public void loadAndSaveDetections(File jsonFile) throws IOException {
        DetectionsWrapper wrapper = objectMapper.readValue(jsonFile, DetectionsWrapper.class);
        
        List<Detection> detections = wrapper.getDetections().stream()
                .map(this::convertDtoToEntity)
                .collect(Collectors.toList());
        
        detectionRepository.saveAll(detections);
        log.info("Saved {} detections to database", detections.size());
    }
    
    private Detection convertDtoToEntity(DetectionDto dto) {
        try {
            return Detection.builder()
                    .timestampMs(dto.getTimestamp_ms())
                    .date(dto.getDate())
                    .objectsTotal(objectMapper.writeValueAsString(dto.getObjects_total()))
                    .objectsByLane(objectMapper.writeValueAsString(dto.getObjects_by_lane()))
                    .avgSpeedByLane(objectMapper.writeValueAsString(dto.getAvg_speed_by_lane()))
                    .build();
        } catch (Exception e) {
            log.error("Error converting DTO to entity", e);
            return new Detection();
        }
    }
}