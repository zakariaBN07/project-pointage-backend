package com.example.pointage_backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectMetricsDTO {
    private String projectId;
    private String affaireNumero;
    private String name;
    private String username;
    private List<String> superviseurIds;

    private BigDecimal plannedHours;
    private BigDecimal consumedHours;
    private BigDecimal remainingHours;

    // percentages as BigDecimal with scale
    private BigDecimal progressPercent;
    private BigDecimal timePercent;

    private boolean isMonitored;
    private boolean timeExceedsProgress;

    // ─────────────────────────────────────────────────────────────────────────
    // New Affaire Fields
    // ─────────────────────────────────────────────────────────────────────────
    private String chrono;
    private String siteDvente;
    private String denomination;
    private String client;
    private String designationClient;
    private String referenceClient;
    private String interlocuteur;
    private String nExonerationTVA;
    private Integer version;
    private Integer avenant;
    private String devise;
    private String chargeAffaires;
    private LocalDate dateOuverture;
    private LocalDate dateConclusion;
    private String derniereEtapeRevolue;
    private LocalDate depuisLe;
    private Boolean reconducteAffaire;
    private Integer nombreDevis;
    private BigDecimal montantEstime;
    private BigDecimal coutFournitures;
    private BigDecimal margePrevisionelle;
    private Integer probabiliteLancementProjet;
}
