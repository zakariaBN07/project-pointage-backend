package com.example.pointage_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "projects")
public class Project {

    @Id
    private String id;

    // affaireNumero from Excel / legacy project number
    private String affaireNumero;

    // human friendly name
    private String name;

    // owner username
    private String username;

    // planned total hours
    private BigDecimal plannedHours;

    // optional deadline
    private LocalDate deadline;

    private Long createdAt;
    private Long updatedAt;

    // ─────────────────────────────────────────────────────────────────────────
    // Affaire identification fields
    // ─────────────────────────────────────────────────────────────────────────

    /** Chrono — unique auto-generated ID for this affaire */
    private String chrono;

    /** Site de vente */
    private String siteDvente;

    /** Dénomination — affaire slogan / title */
    private String denomination;

    /** Client code */
    private String client;

    /** Désignation Client — full client name / description */
    private String designationClient;

    /** Référence client */
    private String referenceClient;

    /** Interlocuteur code */
    private String interlocuteur;

    /** N° Exonération TVA */
    private String nExonerationTVA;

    // ─────────────────────────────────────────────────────────────────────────
    // Autres (version / revision)
    // ─────────────────────────────────────────────────────────────────────────

    /** Version */
    private Integer version;

    /** Avenant */
    private Integer avenant;

    /** Devise (currency code, e.g. EUR) */
    private String devise;

    // ─────────────────────────────────────────────────────────────────────────
    // Responsable & Dates
    // ─────────────────────────────────────────────────────────────────────────

    /** Chargé d'affaires code */
    private String chargeAffaires;

    /** Date d'ouverture */
    private LocalDate dateOuverture;

    /** Date de conclusion */
    private LocalDate dateConclusion;

    /** Dernière étape révolue */
    private String derniereEtapeRevolue;

    /** Depuis le */
    private LocalDate depuisLe;

    /** Reconduire affaire */
    private Boolean reconducteAffaire;

    /** Nombre de devis */
    private Integer nombreDevis;

    // ─────────────────────────────────────────────────────────────────────────
    // Éléments financiers
    // ─────────────────────────────────────────────────────────────────────────

    /** Montant estimé */
    private BigDecimal montantEstime;

    /** Coût fournitures */
    private BigDecimal coutFournitures;

    /** Marge prévisionnelle */
    private BigDecimal margePrevisionelle;

    /** Probabilité lancement du projet client (%) */
    private Integer probabiliteLancementProjet;
}
