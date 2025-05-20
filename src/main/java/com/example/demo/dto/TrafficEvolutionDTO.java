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
public class TrafficEvolutionDTO {
    private List<String> timestamps;
    private List<Integer> car;
    private List<Integer> bus;
    private List<Integer> truck;
}