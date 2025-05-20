package com.example.demo.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeedEvolutionDTO {
    private List<String> timestamps;
    private List<Double> lane_1;
    private List<Double> lane_2;
    private List<Double> lane_3;
}