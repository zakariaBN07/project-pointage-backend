package com.example.pointage_backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMetricsDTO {
    private String projectId;
    private String affaireNumero;
    private String name;
    private List<String> superviseurIds;

    private BigDecimal plannedHours;
    private BigDecimal consumedHours;
    private BigDecimal remainingHours;

    // percentages as BigDecimal with scale
    private BigDecimal progressPercent;
    private BigDecimal timePercent;

    private boolean timeExceedsProgress;
}
