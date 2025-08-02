package com.railse.hiring.workforcemgmt.repository;

import com.railse.hiring.workforcemgmt.model.enums.TaskActivity;

import java.util.List;
import java.util.Optional;

public interface TaskActivityRepository {
    TaskActivity save(TaskActivity activity);
    List<TaskActivity> findByTaskIdOrderByTimestampAsc(Long taskId);
    Optional<TaskActivity> findById(Long id);
    List<TaskActivity> findAll();
}
