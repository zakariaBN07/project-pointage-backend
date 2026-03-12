package com.example.pointage_backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskCreateDTO {
    private String projectId;
    
    private String name;
    private BigDecimal weightPercent;

}
