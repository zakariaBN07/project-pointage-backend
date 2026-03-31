package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByName(String name);
    List<Employee> findByEmail(String email);
    List<Employee> findByMatricule(String matricule);
    List<Employee> findByProjectId(Long projectId);
    List<Employee> findByChargeDAffaireId(Long chargeDAffaireId);
    List<Employee> findByChargeDAffaireIdAndEmail(Long chargeDAffaireId, String email);
    List<Employee> findByChargeDAffaireIdAndMatricule(Long chargeDAffaireId, String matricule);
    List<Employee> findByIngenieurId(Long ingenieurId);
    List<Employee> findByAffaireNumero(String affaireNumero);
}
