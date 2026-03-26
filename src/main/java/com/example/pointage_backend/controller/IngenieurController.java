package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.DailyStatsDTO;
import com.example.pointage_backend.dto.EmployeeDTO;
import com.example.pointage_backend.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/ingenieur")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class IngenieurController {

    private final EmployeeService employeeService;

    @GetMapping("/employees")
    public List<EmployeeDTO> listEmployees(
            @RequestParam(name = "ingenieurId", required = false) Long ingenieurId
    ) {
        return employeeService.getEmployeesByFilter(null, ingenieurId);
    }
}
