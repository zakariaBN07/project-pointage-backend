package com.example.pointage_backend.model;

import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "gestionnaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gestionnaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;

    private String email;
    
    private String role;
    
    private String password;

    private String siege;

    /** Only relevant when role = "ingenieur". Values: ELECTRONIQUE, AUTOMATISME, CHANTIER */
    private String typeIngenieur;
}
