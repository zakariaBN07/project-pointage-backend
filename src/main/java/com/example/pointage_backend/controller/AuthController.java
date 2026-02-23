package com.example.pointage_backend.controller;

import com.example.pointage_backend.dto.*;
import com.example.pointage_backend.model.Gestionnaire;
import com.example.pointage_backend.model.PasswordResetToken;
import com.example.pointage_backend.repository.GestionnaireRepository;
import com.example.pointage_backend.repository.PasswordResetTokenRepository;
import com.example.pointage_backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private GestionnaireRepository gestionnaireRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private EmailService emailService;

    @Value("${password.reset.token.expiration:30}")
    private long tokenExpirationMinutes;

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

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        Optional<Gestionnaire> optionalGestionnaire = gestionnaireRepository.findByName(request.getUsername());

        if (!optionalGestionnaire.isPresent()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Username not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        Gestionnaire gestionnaire = optionalGestionnaire.get();
        
        if (gestionnaire.getEmail() == null || gestionnaire.getEmail().trim().isEmpty()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "User email not configured");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
        
        String resetToken = UUID.randomUUID().toString();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryTime = now.plusMinutes(tokenExpirationMinutes);

        PasswordResetToken token = PasswordResetToken.builder()
                .token(resetToken)
                .gestionnaireId(gestionnaire.getId())
                .email(gestionnaire.getEmail())
                .createdAt(now)
                .expiresAt(expiryTime)
                .used(false)
                .build();

        passwordResetTokenRepository.save(token);

        boolean emailSent = emailService.sendPasswordResetEmail(gestionnaire.getEmail(), resetToken, gestionnaire.getName());

        ForgotPasswordResponse response = ForgotPasswordResponse.builder()
                .success(emailSent)
                .message(emailSent ? "Password reset email sent successfully" : "Password reset token generated (email sending failed - check server logs)")
                .resetToken(resetToken)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        Optional<PasswordResetToken> optionalToken = passwordResetTokenRepository.findByToken(request.getToken());

        if (!optionalToken.isPresent()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid reset token");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        PasswordResetToken resetToken = optionalToken.get();

        if (resetToken.isUsed()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Reset token has already been used");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        if (LocalDateTime.now().isAfter(resetToken.getExpiresAt())) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Reset token has expired");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }

        Optional<Gestionnaire> optionalGestionnaire = gestionnaireRepository.findById(resetToken.getGestionnaireId());

        if (!optionalGestionnaire.isPresent()) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "User not found");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        Gestionnaire gestionnaire = optionalGestionnaire.get();
        gestionnaire.setPassword(request.getNewPassword());
        gestionnaireRepository.save(gestionnaire);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset successfully");
        return ResponseEntity.ok(response);
    }
}
