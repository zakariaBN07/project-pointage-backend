package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.EmployeeDTO;
import com.example.pointage_backend.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class EmployeeSelfController {

    private final EmployeeService employeeService;

    /**
     * Simple endpoint for the employee UI to retrieve its own pointage data
     * based on matricule (identifier entered at login).
     */
    @GetMapping("/me")
    public List<EmployeeDTO> getMyPointage(@RequestParam String matricule) {
        return employeeService.getEmployeesByMatricule(matricule);
    }

    /**
     * Allow an employee UI to update its own pointage record (status, times, counters)
     * using the same DTO mapping logic as supervisor/responsable flows.
     */
    @PutMapping("/me/{id}")
    public EmployeeDTO updateMyPointage(@PathVariable String id, @Valid @RequestBody EmployeeDTO dto) {
        dto.setId(id);
        return employeeService.saveEmployee(dto);
    }
}

