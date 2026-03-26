package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.EmployeeDTO;
import com.example.pointage_backend.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/charge-daffaire")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChargeDAffaireController {

    private final EmployeeService employeeService;

    @GetMapping("/employees")
    public List<EmployeeDTO> listEmployees(
            @RequestParam(name = "chargeDAffaireId", required = false) Long chargeDAffaireId
    ) {
        return employeeService.getEmployeesByFilter(chargeDAffaireId, null);
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