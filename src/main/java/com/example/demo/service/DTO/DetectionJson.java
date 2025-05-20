package com.example.demo.service.DTO;

import lombok.Data;
import java.util.Map;

@Data
public class DetectionJson {
    private Long timestamp_ms;
    private String date;
    private Map<String, Integer> objects_total;
    private Map<String, Map<String, Integer>> objects_by_lane;
    private Map<String, Double> avg_speed_by_lane;
}
