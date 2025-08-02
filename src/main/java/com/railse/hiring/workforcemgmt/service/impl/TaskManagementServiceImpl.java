package com.railse.hiring.workforcemgmt.service.impl;

import com.railse.hiring.workforcemgmt.dto.*;
import com.railse.hiring.workforcemgmt.mapper.ITaskActivityMapper;
import com.railse.hiring.workforcemgmt.mapper.ITaskManagementMapper;
import com.railse.hiring.workforcemgmt.model.enums.*;
import com.railse.hiring.workforcemgmt.exception.ResourceNotFoundException;
import com.railse.hiring.workforcemgmt.repository.TaskActivityRepository;
import com.railse.hiring.workforcemgmt.repository.TaskRepository;
import com.railse.hiring.workforcemgmt.service.TaskManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskManagementServiceImpl implements TaskManagementService {

    private final TaskRepository taskRepository;
    private final ITaskManagementMapper taskMapper;
    private final TaskActivityRepository taskActivityRepository;
    private final ITaskActivityMapper taskActivityMapper;

    @Autowired
    public TaskManagementServiceImpl(TaskRepository taskRepository, ITaskManagementMapper taskMapper, TaskActivityRepository taskActivityRepository, ITaskActivityMapper taskActivityMapper) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.taskActivityRepository = taskActivityRepository;
        this.taskActivityMapper = taskActivityMapper;
    }

    @Override
    public TaskManagementDto findTaskById(Long id) {
        TaskManagement task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return taskMapper.modelToDto(task);
    }

    @Override
    public List<TaskManagementDto> createTasks(TaskCreateRequest createRequest) {
        List<TaskManagement> createdTasks = new ArrayList<>();
        for (TaskCreateRequest.RequestItem item : createRequest.getRequests()) {
            TaskManagement newTask = new TaskManagement();
            newTask.setReferenceId(item.getReferenceId());
            newTask.setReferenceType(item.getReferenceType());
            newTask.setTask(item.getTask());
            newTask.setAssigneeId(item.getAssigneeId());
            newTask.setPriority(item.getPriority());
            newTask.setTaskDeadlineTime(item.getTaskDeadlineTime());
            newTask.setStatus(TaskStatus.ASSIGNED);
            newTask.setDescription("New task created.");
            newTask.setDueDate(Instant.ofEpochMilli(item.getTaskDeadlineTime()).atZone(ZoneId.systemDefault()).toLocalDate());
            createdTasks.add(taskRepository.save(newTask));
        }
        return taskMapper.modelListToDtoList(createdTasks);
    }

    @Override
    public List<TaskManagementDto> updateTasks(UpdateTaskRequest updateRequest) {
        List<TaskManagement> updatedTasks = new ArrayList<>();
        for (UpdateTaskRequest.RequestItem item : updateRequest.getRequests()) {
            TaskManagement task = taskRepository.findById(item.getTaskId())
                    .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + item.getTaskId()));

            if (item.getTaskStatus() != null) {
                task.setStatus(item.getTaskStatus());
            }
            if (item.getDescription() != null) {
                task.setDescription(item.getDescription());
            }
            updatedTasks.add(taskRepository.save(task));
        }
        return taskMapper.modelListToDtoList(updatedTasks);
    }

    @Override
    public String assignByReference(AssignByReferenceRequest request) {
        List<Task> applicableTasks = Task.getTasksByReferenceType(request.getReferenceType());

        for (Task taskType : applicableTasks) {
            List<TaskManagement> tasksToCancel = taskRepository.findByReferenceIdAndReferenceTypeAndStatusNot(
                    request.getReferenceId(),
                    request.getReferenceType(),
                    TaskStatus.COMPLETED
            );

            if (!tasksToCancel.isEmpty()) {
                for (TaskManagement taskToCancel : tasksToCancel) {
                    taskToCancel.setStatus(TaskStatus.CANCELLED);
                    taskRepository.save(taskToCancel);

                    TaskActivity cancellationLog = new TaskActivity();
                    cancellationLog.setTaskId(taskToCancel.getId());
                    cancellationLog.setEventType("STATUS_CHANGE");
                    cancellationLog.setMessage("Task was cancelled due to reassignment.");
                    taskActivityRepository.save(cancellationLog);
                }
            }

            TaskManagement newTask = new TaskManagement();
            newTask.setReferenceId(request.getReferenceId());
            newTask.setReferenceType(request.getReferenceType());
            newTask.setTask(taskType);
            newTask.setAssigneeId(request.getAssigneeId());
            newTask.setStatus(TaskStatus.ASSIGNED);
            taskRepository.save(newTask);

            TaskActivity creationLog = new TaskActivity();
            creationLog.setTaskId(newTask.getId());
            creationLog.setEventType("CREATED");
            creationLog.setMessage("Task created and assigned to assignee ID " + request.getAssigneeId());
            taskActivityRepository.save(creationLog);
        }
        return "Tasks assigned successfully for reference " + request.getReferenceId();
    }

    @Override
    public List<TaskManagementDto> fetchTasksByDate(TaskFetchByDateRequest request) {
        // Corrected code: assigneeIds is already a List<Long>, no need for map(Long::parseLong)
        List<Long> assigneeIds = request.getAssigneeIds();

        List<TaskManagement> filteredTasks = taskRepository.findByAssigneeIdInAndStatusNotAndDueDateBetween(
                assigneeIds,
                TaskStatus.CANCELLED,
                Instant.ofEpochMilli(request.getStartDate()).atZone(ZoneId.systemDefault()).toLocalDate(),
                Instant.ofEpochMilli(request.getEndDate()).atZone(ZoneId.systemDefault()).toLocalDate()
        );
        return taskMapper.modelListToDtoList(filteredTasks);
    }

    @Override
    public void updateTaskPriority(Long taskId, Priority newPriority) {
        TaskManagement task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found: " + taskId));

        TaskActivity priorityLog = new TaskActivity();
        priorityLog.setTaskId(taskId);
        priorityLog.setEventType("PRIORITY_CHANGE");
        priorityLog.setMessage("Priority changed from " + task.getPriority() + " to " + newPriority);
        taskActivityRepository.save(priorityLog);

        task.setPriority(newPriority);
        taskRepository.save(task);
    }

    @Override
    public List<TaskManagementDto> fetchTasksByPriority(Priority priority) {
        List<TaskManagement> tasks = taskRepository.findByPriority(priority);
        return taskMapper.modelListToDtoList(tasks);
    }

    @Override
    public Optional<TaskManagementDto> fetchTaskDetailsWithHistory(Long taskId) {
        Optional<TaskManagement> taskOptional = taskRepository.findById(taskId);

        if (taskOptional.isPresent()) {
            TaskManagement task = taskOptional.get();
            List<TaskActivity> activities = taskActivityRepository.findByTaskIdOrderByTimestampAsc(taskId);

            TaskManagementDto taskDto = taskMapper.modelToDto(task);
            List<TaskActivityDto> activityDtos = taskActivityMapper.toDtoList(activities);

            taskDto.setActivityHistory(activityDtos);
            return Optional.of(taskDto);
        }

        return Optional.empty();
    }

    @Override
    public TaskActivityDto addComment(Long taskId, String comment, Long userId) {
        TaskActivity newComment = new TaskActivity();
        newComment.setTaskId(taskId);
        newComment.setUserId(userId);
        newComment.setEventType("COMMENT");
        newComment.setMessage(comment);
        TaskActivity savedActivity = taskActivityRepository.save(newComment);

        return taskActivityMapper.toDto(savedActivity);
    }
}
