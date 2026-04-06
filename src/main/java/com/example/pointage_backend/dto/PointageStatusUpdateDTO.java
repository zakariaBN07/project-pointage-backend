package com.example.pointage_backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class PointageStatusUpdateDTO {
    private List<Long> ids;
    private String status;
    private Long managerId; // The ID of the ingenieur/manager performing the update (optional for Admin)
}
