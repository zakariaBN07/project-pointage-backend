package com.example.pointage_backend.repository;

import com.example.pointage_backend.model.Employee;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmployeeRepository extends MongoRepository<Employee, String> {
}