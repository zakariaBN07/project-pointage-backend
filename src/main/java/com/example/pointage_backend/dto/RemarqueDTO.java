package com.example.pointage_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RemarqueDTO {
    private Long id;
    private Long senderId;
    private String senderName;
    private String senderRole;
    private String receiverRole;
    private Long receiverId;
    private String receiverName;
    private String content;
    private LocalDateTime timestamp;
}
