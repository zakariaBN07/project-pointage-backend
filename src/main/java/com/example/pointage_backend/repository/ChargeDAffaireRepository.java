package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.ChargeDAffaire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ChargeDAffaireRepository extends JpaRepository<ChargeDAffaire, Long> {
    Optional<ChargeDAffaire> findByUsername(String username);
}
