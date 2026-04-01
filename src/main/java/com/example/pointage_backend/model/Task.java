package com.example.pointage_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tasks")
public class Task {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long projectId;
    private String name;

    // percentage weight, e.g. 25.5
    private BigDecimal weightPercent;

    // status: PENDING, COMPLETED, etc.
    private String status;

    private Boolean completed;
    private LocalDateTime completedAt;
    private String completionDescription;
}
