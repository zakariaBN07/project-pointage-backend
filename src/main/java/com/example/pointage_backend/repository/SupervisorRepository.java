package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.Supervisor;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface SupervisorRepository extends MongoRepository<Supervisor, String> {
    Optional<Supervisor> findByUsername(String username);
}
