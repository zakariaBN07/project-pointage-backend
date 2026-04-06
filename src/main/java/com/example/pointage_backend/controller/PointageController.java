package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.PointageCreateDTO;
import com.example.pointage_backend.dto.PointageStatusUpdateDTO;
import com.example.pointage_backend.model.Pointage;
import com.example.pointage_backend.service.PointageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pointages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PointageController {

    private final PointageService pointageService;

    @GetMapping
    public List<Pointage> listPointages(
            @RequestParam(name = "affaireId", required = false) Long affaireId,
            @RequestParam(name = "employeeId", required = false) Long employeeId
    ) {
        return pointageService.getPointages(affaireId, employeeId);
    }

    @GetMapping("/ingenieur/{ingenieurId}")
    public List<Pointage> listPointagesForIngenieur(@PathVariable("ingenieurId") Long ingenieurId) {
        return pointageService.getPointagesForIngenieur(ingenieurId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Pointage createPointage(@RequestBody PointageCreateDTO dto) {
        return pointageService.createPointage(dto);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<Pointage> createPointagesBulk(@RequestBody List<PointageCreateDTO> dtos) {
        return pointageService.createPointagesBulk(dtos);
    }

    @PatchMapping("/status-update")
    public List<Pointage> updateStatus(@RequestBody PointageStatusUpdateDTO dto) {
        return pointageService.updatePointagesStatus(dto.getIds(), dto.getStatus(), dto.getManagerId());
    }
}
