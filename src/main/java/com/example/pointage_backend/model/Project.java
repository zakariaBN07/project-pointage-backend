package com.example.pointage_backend.model;

import lombok.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "affaires")
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code_affaire")
    private String codeAffaire;

    @Column(name = "nom_affaire")
    private String nomAffaire;

    @Column(name = "affaires_code_affaire_unique", unique = true)
    private String affairesCodeAffaireUnique;

    @Column(name = "tiers_x3")
    private String tiersX3;

    @Column(name = "devise")
    private String devise;

    @Column(name = "charge_affaire")
    private String chargeAffaire;

    @Column(name = "categorie")
    private String categorie;

    @Column(name = "date_affaire")
    private LocalDate dateAffaire;

    @Column(name = "statut")
    private String statut;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "heures_estimees")
    private BigDecimal heuresEstimees;
}
