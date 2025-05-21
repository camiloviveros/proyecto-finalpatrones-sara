package com.trafficanalysis.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BottleneckDto {
    private String lane;
    private Double avgSpeed;
    private Integer totalVehicles;
    private Integer heavyVehicles;
}