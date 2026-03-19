package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.Gestionnaire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GestionnaireRepository extends JpaRepository<Gestionnaire, Long> {
    Optional<Gestionnaire> findByName(String string);
}
