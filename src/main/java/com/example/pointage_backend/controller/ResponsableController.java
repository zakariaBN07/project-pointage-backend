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
@RequestMapping("/api/responsable")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ResponsableController {

    private final EmployeeService employeeService;

    @GetMapping("/employees")
    public List<EmployeeDTO> listEmployees(
            @RequestParam(name = "responsableId", required = false) String responsableId
    ) {
        return employeeService.getEmployeesByFilter(null, responsableId);
    }
}
