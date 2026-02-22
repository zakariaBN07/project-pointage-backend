package com.example.pointage_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "gestionnaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Gestionnaire {
    @Id
    private String id;
    
    private String name;
    
    private String role;
    
    private String password;
}
