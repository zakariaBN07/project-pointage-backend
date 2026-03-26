package com.example.pointage_backend.controller;

import com.example.pointage_backend.service.SyncService;
import com.example.pointage_backend.service.SyncService.SyncResult;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SyncController — exposes endpoints for the admin to trigger direct SQL Server sync.
 *
 * POST /api/sync/gestionnaires  → sync gestionnaires from source table
 * POST /api/sync/employees      → sync employees from source table
 * POST /api/sync/all            → sync everything
 * GET  /api/sync/status         → check if sync is enabled
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

    @PostMapping("/all")
    public ResponseEntity<SyncResult> syncAll() {
        SyncResult result = syncService.syncAll();
        return ResponseEntity.ok(result);
    }
}
