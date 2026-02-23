package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.EmployeeDTO;
import com.example.pointage_backend.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supervisor")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SupervisorController {

    private final EmployeeService employeeService;

    @GetMapping("/employees")
    public List<EmployeeDTO> listEmployees(
            @RequestParam(required = false) String supervisorId
    ) {
        return employeeService.getEmployeesByFilter(supervisorId, null);
    }

    @PostMapping("/employees")
    public EmployeeDTO addEmployee(@RequestBody EmployeeDTO dto) {
        return employeeService.saveEmployee(dto);
    }

    @PutMapping("/employees/{id}")
    public EmployeeDTO updateEmployee(
            @PathVariable String id,
            @RequestBody EmployeeDTO dto
    ) {
        dto.setId(id);
        return employeeService.saveEmployee(dto);
    }

    @DeleteMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@PathVariable String id) {
        employeeService.deleteEmployee(id);
    }
}