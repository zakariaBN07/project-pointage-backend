package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface EmployeeRepository extends MongoRepository<Employee, String> {
    List<Employee> findBySupervisorId(String supervisorId);
    List<Employee> findByResponsableId(String responsableId);
    List<Employee> findByProjectId(String projectId);
}