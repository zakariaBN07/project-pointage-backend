package com.example.pointage_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tasks")
public class Task {

    @Id
    private String id;

    private String projectId;
    private String name;

    // percentage weight, e.g. 25.5
    private BigDecimal weightPercent;

    // status: PENDING, COMPLETED, etc.
    private String status;

    private Boolean completed;
    private LocalDateTime completedAt;
}
