package com.example.pointage_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "password_reset_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasswordResetToken {
    @Id
    private String id;
    
    private String token;
    
    private String gestionnaireId;
    
    private String email;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime expiresAt;
    
    private boolean used;
}
