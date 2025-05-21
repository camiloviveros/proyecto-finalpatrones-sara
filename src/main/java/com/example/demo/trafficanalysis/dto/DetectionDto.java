package com.trafficanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DetectionDto {
    private Long timestamp_ms;
    private String date;
    private Map<String, Integer> objects_total;
    private Map<String, Map<String, Integer>> objects_by_lane;
    private Map<String, Double> avg_speed_by_lane;
}