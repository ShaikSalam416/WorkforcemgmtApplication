package com.railse.hiring.workforcemgmt.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskActivityDto {
    private Long id;
    private Long taskId;
    private Long userId;
    private String eventType;
    private String message;
    private LocalDateTime timestamp;
}
