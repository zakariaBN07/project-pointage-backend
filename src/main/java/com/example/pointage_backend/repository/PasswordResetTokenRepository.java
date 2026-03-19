package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByGestionnaireIdAndUsedFalse(Long gestionnaireId);
}
