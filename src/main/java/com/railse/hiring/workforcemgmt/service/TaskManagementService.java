package com.railse.hiring.workforcemgmt.service;

import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.model.enums.Priority;


import java.util.List;
import java.util.Optional;

public interface TaskManagementService {
    List<TaskManagementDto> createTasks(TaskCreateRequest request);
    List<TaskManagementDto> updateTasks(UpdateTaskRequest request);
    String assignByReference(AssignByReferenceRequest request);
    List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request);
    TaskManagementDto findTaskById(Long id);
    void updateTaskPriority(Long taskId, Priority newPriority);
    List<TaskManagementDto> fetchTasksByPriority(Priority priority);
    Optional<TaskManagementDto> fetchTaskDetailsWithHistory(Long taskId);
    TaskActivityDto addComment(Long taskId, String comment, Long userId);


}
