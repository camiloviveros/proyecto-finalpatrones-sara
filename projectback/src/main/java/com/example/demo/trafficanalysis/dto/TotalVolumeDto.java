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
public class TotalVolumeDto {
    private Map<String, Integer> hourly;
    private Map<String, Integer> daily;
    private Map<String, Integer> total;
}