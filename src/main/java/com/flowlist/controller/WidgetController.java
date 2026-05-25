package com.flowlist.controller;

import com.flowlist.entity.Task;
import com.flowlist.entity.User;
import com.flowlist.repository.TaskRepository;
import com.flowlist.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

// Long-lived, read-only widget feed — no JWT. The widget key is a random
// secret in the URL; exposes only task titles/state for the owning user.
@RestController
@RequestMapping("/api/widget")
public class WidgetController {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final UserRepository userRepo;
    private final TaskRepository taskRepo;

    public WidgetController(UserRepository userRepo, TaskRepository taskRepo) {
        this.userRepo = userRepo;
        this.taskRepo = taskRepo;
    }

    // Auth-gated: fetch (or lazily create) this user's widget key.
    @GetMapping("/key")
    @Transactional
    public ResponseEntity<?> getKey(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepo.findByEmail(principal.getUsername())
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        if (user.getWidgetKey() == null || user.getWidgetKey().isBlank()) {
            user.setWidgetKey(UUID.randomUUID().toString().replace("-", ""));
            userRepo.save(user);
        }
        return ResponseEntity.ok(Map.of("key", user.getWidgetKey()));
    }

    // Public: read-only task summary for the widget.
    @GetMapping("/data/{key}")
    public ResponseEntity<?> data(@PathVariable String key) {
        Optional<User> u = userRepo.findByWidgetKey(key);
        if (u.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Invalid widget key"));

        List<Task> tasks = taskRepo.findByUserIdAndCompletedFalse(u.get().getId());
        long today = startOfTodayMs();

        int overdue = 0, dueToday = 0;
        List<Map<String, String>> items = new ArrayList<>();
        // overdue first, then today
        List<Task> sorted = new ArrayList<>(tasks);
        sorted.sort(Comparator.comparing(t -> t.getDueDate() == null ? Instant.MAX : t.getDueDate()));
        for (Task t : sorted) {
            String state = dueState(t, today);
            if (state.equals("overdue")) overdue++;
            else if (state.equals("today")) dueToday++;
            if ((state.equals("overdue") || state.equals("today")) && items.size() < 5) {
                items.add(Map.of("title", t.getTitle(), "state", state,
                        "category", t.getCategory() == null ? "" : t.getCategory()));
            }
        }
        return ResponseEntity.ok(Map.of("overdue", overdue, "today", dueToday, "items", items));
    }

    private long startOfTodayMs() {
        return Instant.now().atZone(IST).toLocalDate().atStartOfDay(IST).toInstant().toEpochMilli();
    }
    private String dueState(Task t, long startOfTodayMs) {
        if (t.getDueDate() == null) return "none";
        long d = t.getDueDate().atZone(IST).toLocalDate().atStartOfDay(IST).toInstant().toEpochMilli();
        if (d < startOfTodayMs) return "overdue";
        if (d == startOfTodayMs) return "today";
        return "future";
    }
}
