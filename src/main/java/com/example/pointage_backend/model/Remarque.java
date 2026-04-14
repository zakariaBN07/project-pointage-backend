package com.example.pointage_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "remarques")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Remarque {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long senderId;
    private String senderName;
    private String senderRole;
    
    // Can be "admin", "charge_affaire", "ingenieur"
    private String receiverRole;
    
    // Nullable, if null, it means it's for everyone in that role
    private Long receiverId;
    private String receiverName;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime timestamp;
}
