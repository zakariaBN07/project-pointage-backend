package com.example.pointage_backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AffaireMetricsDTO {
    private Long id;
    private String codeAffaire;
    private String nomAffaire;
    private String affairesCodeAffaireUnique;
    private String tiersX3;
    private String devise;
    private String chargeAffaire;
    private String categorie;
    private LocalDate dateAffaire;
    private String statut;
    private String description;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    private BigDecimal heuresEstimees;

    private List<Long> chargeDAffaireIds;

    private BigDecimal plannedHours;
    private BigDecimal consumedHours;
    private BigDecimal remainingHours;

    // percentages as BigDecimal with scale
    private BigDecimal progressPercent;
    private BigDecimal timePercent;

    private boolean isMonitored;
    private boolean timeExceedsProgress;
}
