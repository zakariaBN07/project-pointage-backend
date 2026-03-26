package com.example.pointage_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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
    private String chantierAtelier;
    private String client;
    private String name; 

    private Double nbrJrsAbsence;
    private Double nbrJrsConges;
    private Double nbrJrsDeplacementsExpatrie;
    private Double nbrJrsDeplacementsMaroc;
    private Double nbrJrsDetente;
    private Double nbrJrsFeries;
    private Double nbrJrsFeriesTravailes;
    private Double nbrJrsMaladie;
    private Double nbrJrsPaniers;
    private Double nbrJrsRecuperation;
    private Double nbrJrsTravaillees;
    private Double totHrsDimanche;
    private Double totHrsTravaillees;

    private String plannedHours;
    private String pointageEntree;
    private String pointageSortie;
    private Long projectId;
    private Integer projectProgress;
    private Long ingenieurId;
    private String site;
    private String status;
    private Long chargeDAffaireId;

    /**
     * Calculate hours worked from actual attendance data (pointageEntree and pointageSortie).
     * Can be extended to include other attendance breakdown fields if needed.
     * Returns the sum of various hours worked categories.
     */
    public Double calculateHoursWorked() {
        double total = 0.0;
        // Sum all actual hours worked (not including absences, leave, etc.)
        if (this.totHrsTravaillees != null) total += this.totHrsTravaillees;
        if (this.totHrsDimanche != null) total += this.totHrsDimanche;
        if (this.nbrJrsFeriesTravailes != null) total += this.nbrJrsFeriesTravailes;
        // Do not include: absences, congés, déplacements, détente, récupération, maladie
        return total > 0 ? total : 0.0;
    }
}