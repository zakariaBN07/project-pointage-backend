package com.example.pointage_backend.dto;

import lombok.*;
import jakarta.validation.constraints.Min;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private String id;
    private String name;
    private String matricule;
    private String affaireNumero;
    private String projectId;
    private String client;
    private String site;
    private String plannedHours;


    private String status;
    private String pointageEntree;
    private String pointageSortie;

    private String supervisorId;
    private String responsableId;

    @Min(0)
    private Double totHrsTravaillees;
    @Min(0)
    private Double nbrJrsTravaillees;
    @Min(0)
    private Double nbrJrsAbsence;
    @Min(0)
    private Double totHrsDimanche;
    @Min(0)
    private Double nbrJrsFeries;
    @Min(0)
    private Double nbrJrsFeriesTravailes;
    @Min(0)
    private Double nbrJrsConges;
    @Min(0)
    private Double nbrJrsDeplacementsMaroc;
    @Min(0)
    private Double nbrJrsPaniers;
    @Min(0)
    private Double nbrJrsDetente;
    @Min(0)
    private Double nbrJrsDeplacementsExpatrie;
    @Min(0)
    private Double nbrJrsRecuperation;
    @Min(0)
    private Double nbrJrsMaladie;

    private String chantierAtelier;
    private Integer projectProgress;
}