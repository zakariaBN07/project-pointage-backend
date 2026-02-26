package com.example.pointage_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "employees")
public class Employee {

    @Id
    private String id;

    private String name;
    private String matricule;
    private String affaireNumero;
    private String client;
    private String site;

    // ✅ pointage fields
    private String status;           // En attente | Présent | Absent | Sortie
    private String pointageEntree;   // HH:mm:ss
    private String pointageSortie;   // HH:mm:ss

    private String supervisorId;     
    private String responsableId;   

    // Attendance fields
    private Double totHrsTravaillees;
    private Double nbrJrsTravaillees;
    private Double nbrJrsAbsence;
    private Double totHrsDimanche;
    private Double nbrJrsFeries;
    private Double nbrJrsFeriesTravailes;
    private Double nbrJrsConges;
    private Double nbrJrsDeplacementsMaroc;
    private Double nbrJrsPaniers;
    private Double nbrJrsDetente;
    private Double nbrJrsDeplacementsExpatrie;
    private Double nbrJrsRecuperation;
    private Double nbrJrsMaladie;
    private String chantierAtelier;
}