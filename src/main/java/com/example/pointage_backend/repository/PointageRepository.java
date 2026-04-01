package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.Pointage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PointageRepository extends JpaRepository<Pointage, Long> {
    List<Pointage> findByEmployeeIdOrderByDatePointageDesc(Long employeeId);
    List<Pointage> findByAffaireIdOrderByDatePointageDesc(Long affaireId);
    List<Pointage> findByEmployeeIdInOrderByDatePointageDesc(List<Long> employeeIds);
}
