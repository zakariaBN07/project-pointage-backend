package com.example.pointage_backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "charge_daffaires")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChargeDAffaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    @Column(unique = true)
    private String username;
}
