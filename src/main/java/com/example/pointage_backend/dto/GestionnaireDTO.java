package com.example.pointage_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GestionnaireDTO {
    private String id;
    private String name;
    private String role;
    private String password;
}
