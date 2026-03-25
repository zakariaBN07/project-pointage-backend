package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.Affaire;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AffaireRepository extends JpaRepository<Affaire, Long> {
    Optional<Affaire> findByCodeAffaire(String codeAffaire);
    Optional<Affaire> findByAffairesCodeAffaireUnique(String affairesCodeAffaireUnique);
}
