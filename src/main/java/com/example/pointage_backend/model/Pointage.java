package com.example.pointage_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pointage")
public class Pointage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;

    @Column(name = "affaire_id", nullable = false)
    private Long affaireId;

    @Column(name = "mois_poste", nullable = false)
    private String moisPoste;

    @Column(name = "date_pointage", nullable = false)
    private LocalDate datePointage;

    @Column(name = "heures_travaillees", nullable = false, precision = 8, scale = 2)
    private BigDecimal heuresTravaillees;

    @Column(name = "type_activite")
    private String typeActivite;

    @Lob
    @Column(name = "description")
    private String description;

    @Column(name = "cout_mad", nullable = false, precision = 15, scale = 2)
    private BigDecimal coutMad;

    @Column(name = "vente_mad", nullable = false, precision = 15, scale = 2)
    private BigDecimal venteMad;

    @Column(name = "devise_achat", nullable = false)
    private String deviseAchat;

    @Column(name = "devise_vente", nullable = false)
    private String deviseVente;

    @Column(name = "cout_eur", nullable = false, precision = 15, scale = 2)
    private BigDecimal coutEur;

    @Column(name = "vent_eur", nullable = false, precision = 15, scale = 2)
    private BigDecimal ventEur;

    @Column(name = "statut", nullable = false)
    private String statut;

    @Column(name = "valide_par")
    private Long validePar;

    @Column(name = "valide_at")
    private LocalDateTime valideAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (heuresTravaillees == null) heuresTravaillees = BigDecimal.ZERO;
        if (coutMad == null) coutMad = BigDecimal.ZERO;
        if (venteMad == null) venteMad = BigDecimal.ZERO;
        if (coutEur == null) coutEur = BigDecimal.ZERO;
        if (ventEur == null) ventEur = BigDecimal.ZERO;
        if (deviseAchat == null || deviseAchat.isBlank()) deviseAchat = "MAD";
        if (deviseVente == null || deviseVente.isBlank()) deviseVente = "MAD";
        if (statut == null || statut.isBlank()) statut = "EN_ATTENTE";
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
