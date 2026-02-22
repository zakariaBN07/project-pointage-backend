package com.example.pointage_backend.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String name;
    private String password;
}
