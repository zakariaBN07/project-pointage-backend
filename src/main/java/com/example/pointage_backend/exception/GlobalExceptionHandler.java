package com.example.pointage_backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        System.err.println("IllegalArgumentException: " + ex.getMessage());
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        ex.printStackTrace();
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(org.springframework.dao.DataIntegrityViolationException ex) {
        String message = ex.getMostSpecificCause().getMessage();
        System.err.println("Database Integrity Violation: " + message);
        
        Map<String, String> response = new HashMap<>();
        String userFriendlyMessage = "Erreur de base de données : une contrainte d'unicité a été violée.";
        
        if (message != null) {
            if (message.contains("affaires_code_affaire_unique")) {
                userFriendlyMessage = "Ce 'Chrono' (Code Unique) existe déjà. Veuillez recharger la page pour en générer un nouveau.";
            } else if (message.contains("code_affaire") || message.contains("affaire_numero")) {
                userFriendlyMessage = "Ce numéro d'affaire est déjà utilisé pour un autre projet.";
            } else {
                userFriendlyMessage = "Violation de contrainte : " + message;
            }
        }
        
        response.put("error", userFriendlyMessage);
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex) {
        ex.printStackTrace();
        Map<String, String> response = new HashMap<>();
        String message = ex.getMessage();
        if (ex.getCause() != null) {
            message += " | Cause: " + ex.getCause().getMessage();
        }
        response.put("error", "Erreur Interne [" + ex.getClass().getSimpleName() + "]: " + message);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
