package com.example.pointage_backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String token;
    
    private Long gestionnaireId;
    
    private String email;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime expiresAt;
    
    private boolean used;
}
