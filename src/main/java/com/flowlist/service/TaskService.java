package com.flowlist.service;

import com.flowlist.dto.*;
import com.flowlist.entity.Task;
import com.flowlist.entity.User;
import com.flowlist.repository.TaskRepository;
import com.flowlist.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public List<TaskResponse> getAll(String email) {
        return taskRepository.findByUserIdOrderByCreatedAtDesc(getUser(email).getId())
                .stream().map(TaskResponse::from).toList();
    }

    @Transactional
    public TaskResponse create(String email, TaskRequest req) {
        User user = getUser(email);
        Task task = Task.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .title(req.getTitle().trim())
                .dueDate(req.getDueDate())
                .category(req.getCategory())
                .completed(false)
                .build();
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse update(String email, String taskId, TaskRequest req) {
        Task task = ownerTask(email, taskId);
        task.setTitle(req.getTitle().trim());
        task.setDueDate(req.getDueDate());
        task.setCategory(req.getCategory());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse complete(String email, String taskId) {
        Task task = ownerTask(email, taskId);
        task.setCompleted(true);
        task.setCompletedAt(Instant.now());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse uncomplete(String email, String taskId) {
        Task task = ownerTask(email, taskId);
        task.setCompleted(false);
        task.setCompletedAt(null);
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public void delete(String email, String taskId) {
        ownerTask(email, taskId);  // verify ownership
        taskRepository.deleteByIdAndUserId(taskId, getUser(email).getId());
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Task ownerTask(String email, String taskId) {
        Long userId = getUser(email).getId();
        return taskRepository.findByIdAndUserId(taskId, userId)
                .orElseThrow(() -> new AccessDeniedException("Task not found or access denied"));
    }
}
