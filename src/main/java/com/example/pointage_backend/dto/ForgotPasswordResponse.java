package com.example.pointage_backend.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForgotPasswordResponse {
    private boolean success;
    private String message;
    private String resetToken;
}
