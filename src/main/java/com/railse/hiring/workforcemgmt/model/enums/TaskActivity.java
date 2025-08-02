package com.railse.hiring.workforcemgmt.model.enums;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TaskActivity {
    private Long id;
    private Long taskId; // The ID of the task this activity belongs to
    private Long userId; // The user who performed the action or left the comment
    private String eventType; // e.g., "COMMENT", "ASSIGNED", "PRIORITY_CHANGE"
    private String message; // The details of the activity or the comment text
    private LocalDateTime timestamp;
}
