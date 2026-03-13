package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends MongoRepository<Project, String> {
    Optional<Project> findFirstByAffaireNumero(String affaireNumero);
    List<Project> findByAffaireNumero(String affaireNumero);
}
