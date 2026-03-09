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
}
