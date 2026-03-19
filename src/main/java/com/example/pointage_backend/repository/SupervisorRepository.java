package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.Supervisor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SupervisorRepository extends JpaRepository<Supervisor, Long> {
    Optional<Supervisor> findByUsername(String username);
}
