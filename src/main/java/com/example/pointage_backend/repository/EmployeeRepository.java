package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByName(String name);
    List<Employee> findByEmail(String email);
    List<Employee> findByProjectId(Long projectId);
    List<Employee> findBySupervisorId(Long supervisorId);
    List<Employee> findByResponsableId(Long responsableId);
    List<Employee> findByAffaireNumero(String affaireNumero);
}
