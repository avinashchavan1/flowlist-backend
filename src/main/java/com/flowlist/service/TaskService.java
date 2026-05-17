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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    // Hard cap on series length — prevents accidental thousands-of-rows insert if user picks a huge range
    private static final int MAX_SERIES_OCCURRENCES = 365;

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
    public List<TaskResponse> create(String email, TaskRequest req) {
        User user = getUser(email);

        // ── Series creation: recurrence + start + end all present ──
        if (req.getRecurrence() != null && !req.getRecurrence().isBlank()
                && req.getRecurrenceStart() != null && req.getRecurrenceEnd() != null) {
            List<Task> series = buildSeries(user, req);
            return taskRepository.saveAll(series)
                    .stream().map(TaskResponse::from).toList();
        }

        // ── Single task (no recurrence range) ──
        Task task = Task.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .title(req.getTitle().trim())
                .notes(req.getNotes())
                .dueDate(req.getDueDate())
                .category(req.getCategory())
                .priority(req.getPriority())
                .recurrence(req.getRecurrence())
                .completed(false)
                .build();
        return List.of(TaskResponse.from(taskRepository.save(task)));
    }

    @Transactional
    public TaskResponse update(String email, String taskId, TaskRequest req) {
        Task task = ownerTask(email, taskId);
        task.setTitle(req.getTitle().trim());
        task.setNotes(req.getNotes());
        task.setDueDate(req.getDueDate());
        task.setCategory(req.getCategory());
        task.setPriority(req.getPriority());
        task.setRecurrence(req.getRecurrence());
        return TaskResponse.from(taskRepository.save(task));
    }

    @Transactional
    public TaskResponse complete(String email, String taskId) {
        Task task = ownerTask(email, taskId);
        task.setCompleted(true);
        task.setCompletedAt(Instant.now());
        TaskResponse response = TaskResponse.from(taskRepository.save(task));

        // Series tasks: all occurrences are already in the DB — do not auto-spawn.
        if (task.getRecurrenceStart() != null) return response;

        // Old-style single recurring task: spawn the next occurrence on complete.
        if (task.getRecurrence() != null && !task.getRecurrence().isBlank() && task.getDueDate() != null) {
            Instant nextDue = calculateNextDue(task.getDueDate(), task.getRecurrence());
            Task next = Task.builder()
                    .id(UUID.randomUUID().toString())
                    .user(task.getUser())
                    .title(task.getTitle())
                    .notes(task.getNotes())
                    .dueDate(nextDue)
                    .category(task.getCategory())
                    .priority(task.getPriority())
                    .recurrence(task.getRecurrence())
                    .completed(false)
                    .build();
            taskRepository.save(next);
        }

        return response;
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
        // deleteByIdAndUserId enforces ownership via the userId predicate — no separate ownerTask call needed
        taskRepository.deleteByIdAndUserId(taskId, getUser(email).getId());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Generate one Task per occurrence between recurrenceStart and recurrenceEnd (inclusive),
     * stepping by the recurrence interval. Each task gets recurrenceStart set so that
     * complete() knows not to auto-spawn.
     */
    private List<Task> buildSeries(User user, TaskRequest req) {
        ZonedDateTime cursor = req.getRecurrenceStart().atZone(IST);
        ZonedDateTime end    = req.getRecurrenceEnd().atZone(IST);
        Instant seriesStart  = req.getRecurrenceStart();

        List<Task> list = new ArrayList<>();
        while (!cursor.isAfter(end) && list.size() < MAX_SERIES_OCCURRENCES) {
            list.add(Task.builder()
                    .id(UUID.randomUUID().toString())
                    .user(user)
                    .title(req.getTitle().trim())
                    .notes(req.getNotes())
                    .dueDate(cursor.toInstant())
                    .category(req.getCategory())
                    .priority(req.getPriority())
                    .recurrence(req.getRecurrence())
                    .recurrenceStart(seriesStart)   // marks this as a series task
                    .completed(false)
                    .build());

            cursor = switch (req.getRecurrence().toLowerCase()) {
                case "weekly"         -> cursor.plusWeeks(1);
                case "monthly"        -> cursor.plusMonths(1);
                case "alternate_days" -> cursor.plusDays(2);
                default               -> cursor.plusDays(1);  // daily
            };
        }
        return list;
    }

    private Instant calculateNextDue(Instant current, String recurrence) {
        ZonedDateTime zdt = current.atZone(IST);
        return switch (recurrence.toLowerCase()) {
            case "weekly"  -> zdt.plusWeeks(1).toInstant();
            case "monthly" -> zdt.plusMonths(1).toInstant();
            default        -> zdt.plusDays(1).toInstant();
        };
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
