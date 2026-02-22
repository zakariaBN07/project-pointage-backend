package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.GestionnaireDTO;
import com.example.pointage_backend.model.Gestionnaire;
import com.example.pointage_backend.repository.GestionnaireRepository;
import com.example.pointage_backend.service.GestionnaireService;
import com.example.pointage_backend.service.ExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminController {
    private final GestionnaireService gestionnaireService;
    private final ExcelService excelService;
    private final GestionnaireRepository gestionnaireRepository;

    @GetMapping("/gestionnaires")
    public List<GestionnaireDTO> listGestionnaires() {
        return gestionnaireService.getAllGestionnaires();
    }

    @PostMapping("/gestionnaires")
    public GestionnaireDTO addGestionnaire(@RequestBody GestionnaireDTO dto) {
        return gestionnaireService.saveGestionnaire(dto);
    }

    @PutMapping("/gestionnaires/{id}")
    public GestionnaireDTO updateGestionnaire(@PathVariable String id, @RequestBody GestionnaireDTO dto) {
        dto.setId(id);
        return gestionnaireService.saveGestionnaire(dto);
    }

    @DeleteMapping("/gestionnaires/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGestionnaire(@PathVariable String id) {
        gestionnaireService.deleteGestionnaire(id);
    }

    @GetMapping("/gestionnaires/export")
    public ResponseEntity<byte[]> exportGestionnaires() throws IOException {
        List<Gestionnaire> gestionnaires = gestionnaireRepository.findAll();
        byte[] excel = excelService.exportGestionnairesToExcel(gestionnaires);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=gestionnaires.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(excel);
    }
}
