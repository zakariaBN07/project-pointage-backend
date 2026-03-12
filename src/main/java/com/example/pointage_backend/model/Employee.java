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
    private String projectId;
    private String client;
    private String site;
    private String plannedHours;

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
    private Integer projectProgress;

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