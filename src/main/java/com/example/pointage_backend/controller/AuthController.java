package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.LoginRequest;
import com.example.pointage_backend.dto.LoginResponse;
import com.example.pointage_backend.model.Gestionnaire;
import com.example.pointage_backend.repository.GestionnaireRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private GestionnaireRepository gestionnaireRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<Gestionnaire> optionalGestionnaire = gestionnaireRepository.findByName(loginRequest.getName());

        if (optionalGestionnaire.isPresent()) {
            Gestionnaire gestionnaire = optionalGestionnaire.get();
            // In a real application, you should use password hashing (e.g., BCrypt)
            if (gestionnaire.getPassword().equals(loginRequest.getPassword())) {
                LoginResponse response = LoginResponse.builder()
                        .id(gestionnaire.getId())
                        .name(gestionnaire.getName())
                        .role(gestionnaire.getRole())
                        .build();
                return ResponseEntity.ok(response);
            }
        }

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "Invalid credentials");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
}
