package com.trafficanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SpeedEvolutionDto {
    private List<String> timestamps;
    private List<Double> lane_1;
    private List<Double> lane_2;
    private List<Double> lane_3;
}