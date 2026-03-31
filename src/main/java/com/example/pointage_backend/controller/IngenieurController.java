package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.EmployeeDTO;
import com.example.pointage_backend.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ingenieur")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IngenieurController {

    private final EmployeeService employeeService;

    @GetMapping("/employees")
    public List<EmployeeDTO> listEmployees(
            @RequestParam(name = "ingenieurId", required = false) Long ingenieurId,
            @RequestParam(name = "chargeDAffaireId", required = false) Long chargeDAffaireId
    ) {
        if (chargeDAffaireId != null) {
            return employeeService.getEmployeesByFilter(chargeDAffaireId, null);
        }
        return employeeService.getEmployeesByFilter(null, ingenieurId);
    }

    @PostMapping("/employees")
    public EmployeeDTO addEmployee(@Valid @RequestBody EmployeeDTO dto) {
        return employeeService.saveEmployee(dto);
    }

    @PutMapping("/employees/{id}")
    public EmployeeDTO updateEmployee(
            @PathVariable("id") Long id,
            @Valid @RequestBody EmployeeDTO dto
    ) {
        dto.setId(id);
        return employeeService.saveEmployee(dto);
    }

    @DeleteMapping("/employees/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteEmployee(@PathVariable("id") Long id) {
        employeeService.deleteEmployee(id);
    }
}

