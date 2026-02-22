package com.example.pointage_backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDTO {

    private String id;
    private String name;
    private String matricule;

    private String status;
    private String pointageEntree;
    private String pointageSortie;

    private String supervisorId;
}