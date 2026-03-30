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
     * based on email (identifier entered at login).
     */
    @GetMapping("/me")
    public List<EmployeeDTO> getMyPointage(@RequestParam(name = "email") String email) {
        return employeeService.getEmployeesByEmail(email);
    }

    /**
     * Allow an employee UI to update its own pointage record (status, times, counters)
     * using the same DTO mapping logic as Ingénieur/Chargé d'affaires flows.
     */
    @PutMapping("/me/{id}")
    public EmployeeDTO updateMyPointage(@PathVariable("id") Long id, @Valid @RequestBody EmployeeDTO dto) {
        dto.setId(id);
        return employeeService.saveEmployee(dto);
    }
}

