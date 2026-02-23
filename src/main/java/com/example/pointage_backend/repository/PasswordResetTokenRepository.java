package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {
    Optional<PasswordResetToken> findByToken(String token);
    Optional<PasswordResetToken> findByGestionnaireIdAndUsedFalse(String gestionnaireId);
}
