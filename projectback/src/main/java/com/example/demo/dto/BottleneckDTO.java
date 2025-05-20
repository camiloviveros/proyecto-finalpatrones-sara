package com.example.demo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BottleneckDTO {
    private String lane;
    private Double avgSpeed;
    private Integer totalVehicles;
    private Integer heavyVehicles;
}