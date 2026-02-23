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

    // ✅ pointage fields
    private String status;           // En attente | Présent | Absent | Sortie
    private String pointageEntree;   // HH:mm:ss
    private String pointageSortie;   // HH:mm:ss

    private String supervisorId;     
    private String responsableId;   
}