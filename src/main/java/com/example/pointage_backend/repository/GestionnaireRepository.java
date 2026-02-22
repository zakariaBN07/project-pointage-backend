package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.Gestionnaire;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface GestionnaireRepository extends MongoRepository<Gestionnaire, String> {
    Optional<Gestionnaire> findByName(String name);
}
