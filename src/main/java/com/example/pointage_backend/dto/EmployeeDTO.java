package com.example.pointage_backend.dto;

import lombok.*;
import jakarta.validation.constraints.Min;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String matricule;
    private String email;
    private String post;
    private String departement;
    private Double tauxHoraire;
    private String deviseTaux;
    private Boolean actif;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;

    private String affaireNumero;
    private String affaireIntitule; // Keep this one as it's computed/extra info
    private String chantierAtelier;
    private String client;
    private String name;

    @Min(0) private Double nbrJrsAbsence;
    @Min(0) private Double nbrJrsConges;
    @Min(0) private Double nbrJrsDeplacementsExpatrie;
    @Min(0) private Double nbrJrsDeplacementsMaroc;
    @Min(0) private Double nbrJrsDetente;
    @Min(0) private Double nbrJrsFeries;
    @Min(0) private Double nbrJrsFeriesTravailes;
    @Min(0) private Double nbrJrsMaladie;
    @Min(0) private Double nbrJrsPaniers;
    @Min(0) private Double nbrJrsRecuperation;
    @Min(0) private Double nbrJrsTravaillees;
    @Min(0) private Double totHrsDimanche;
    @Min(0) private Double totHrsTravaillees;

    private String plannedHours;
    private String pointageEntree;
    private String pointageSortie;
    private Long projectId;
    private Integer projectProgress;
    private Long responsableId;
    private String site;
    private String status;
    private Long supervisorId;
}