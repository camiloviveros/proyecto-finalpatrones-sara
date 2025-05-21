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
public class TrafficEvolutionDto {
    private List<String> timestamps;
    private List<Integer> car;
    private List<Integer> bus;
    private List<Integer> truck;
}