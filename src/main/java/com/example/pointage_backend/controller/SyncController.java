package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.EmployeeDTO;
import com.example.pointage_backend.service.SyncService;
import com.example.pointage_backend.service.SyncService.SyncResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * SyncController — exposes endpoints for the admin to trigger direct SQL Server sync.
 *
 * POST /api/sync/employees          → sync all employees from source table
 * GET  /api/sync/employees/available → preview employees from source
 * POST /api/sync/employees/selective → sync specific employees by matricules
 * POST /api/sync/all                → sync everything
 * GET  /api/sync/status             → check if sync is enabled
 */
@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/gestionnaires")
    public ResponseEntity<SyncResult> syncGestionnaires() {
        SyncResult result = syncService.syncGestionnaires();
        return ResponseEntity.ok(result);
    }

    @PostMapping("/employees")
    public ResponseEntity<SyncResult> syncEmployees(
            @RequestParam(required = false) Long chargeDAffaireId,
            @RequestParam(required = false) Long ingenieurId) {
        SyncResult result = syncService.syncEmployees(chargeDAffaireId, ingenieurId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/employees/available")
    public ResponseEntity<List<EmployeeDTO>> getAvailableEmployees() {
        return ResponseEntity.ok(syncService.getAvailableEmployeesFromSource());
    }

    @PostMapping("/employees/selective")
    public ResponseEntity<SyncResult> syncSelectedEmployees(@RequestBody SelectiveSyncRequest request) {
        SyncResult result = syncService.syncSelectedEmployees(request.getMatricules(), request.getIngenieurId());
        return ResponseEntity.ok(result);
    }

    @lombok.Data
    public static class SelectiveSyncRequest {
        private List<String> matricules;
        private Long ingenieurId;
    }

    @PostMapping("/all")
    public ResponseEntity<SyncResult> syncAll() {
        SyncResult result = syncService.syncAll();
        return ResponseEntity.ok(result);
    }
}
