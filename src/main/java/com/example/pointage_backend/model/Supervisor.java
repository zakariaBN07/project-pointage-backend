package com.example.pointage_backend.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "supervisors")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supervisor {
    @Id
    private String id;
    
    private String name;
    
    @Indexed(unique = true)
    private String username;
}
