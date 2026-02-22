package com.example.pointage_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyStatsDTO {
    private long totalGestionnaires;
    private long presentGestionnaires;
    private long absentGestionnaires;
}
