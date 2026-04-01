package com.example.pointage_backend.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointageCreateDTO {
    private Long employeeId;
    private Long affaireId;
    private String moisPoste;
    private LocalDate datePointage;
    private BigDecimal heuresTravaillees;
    private String typeActivite;
    private String description;
    private BigDecimal coutMad;
    private BigDecimal venteMad;
    private String deviseAchat;
    private String deviseVente;
    private BigDecimal coutEur;
    private BigDecimal ventEur;
    private String statut;
    private Long validePar;
}
