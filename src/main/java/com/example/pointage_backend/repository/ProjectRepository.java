package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    Optional<Project> findByCodeAffaire(String codeAffaire);
}
