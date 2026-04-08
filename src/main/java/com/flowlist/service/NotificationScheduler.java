package com.flowlist.service;

import com.flowlist.entity.PushSubscription;
import com.flowlist.entity.Task;
import com.flowlist.repository.PushSubscriptionRepository;
import com.flowlist.repository.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);
    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("h:mm a").withZone(ZoneId.systemDefault());

    private final PushSubscriptionRepository subRepo;
    private final TaskRepository taskRepo;
    private final PushNotificationService pushService;

    public NotificationScheduler(PushSubscriptionRepository subRepo,
                                  TaskRepository taskRepo,
                                  PushNotificationService pushService) {
        this.subRepo = subRepo;
        this.taskRepo = taskRepo;
        this.pushService = pushService;
    }

    // ── Due Soon: every 5 minutes, check for tasks due in 25–35 minutes ──
    @Scheduled(fixedRate = 300_000)
    public void checkDueSoon() {
        Instant windowStart = Instant.now().plusSeconds(25 * 60);
        Instant windowEnd   = Instant.now().plusSeconds(35 * 60);

        List<PushSubscription> subs = subRepo.findDueSoonSubscribers();
        for (PushSubscription sub : subs) {
            Long userId = sub.getUser().getId();
            List<Task> tasks = taskRepo.findByUserIdOrderByCreatedAtDesc(userId);
            for (Task task : tasks) {
                if (task.isCompleted() || task.getDueDate() == null) continue;
                Instant due = task.getDueDate();
                if (due.isAfter(windowStart) && due.isBefore(windowEnd)) {
                    String timeStr = TIME_FMT.format(due);
                    pushService.sendNotification(sub,
                        "Due in 30 minutes",
                        task.getTitle() + " is due at " + timeStr,
                        "/?task=" + task.getId(),
                        "due_soon"
                    );
                }
            }
        }
    }

    // ── Overdue: every 15 minutes, alert for newly overdue tasks ──
    @Scheduled(fixedRate = 900_000)
    public void checkOverdue() {
        Instant now = Instant.now();
        // Check tasks that became overdue in the last 15 minutes
        Instant windowStart = now.minusSeconds(15 * 60);

        List<PushSubscription> subs = subRepo.findOverdueSubscribers();
        for (PushSubscription sub : subs) {
            Long userId = sub.getUser().getId();
            List<Task> tasks = taskRepo.findByUserIdOrderByCreatedAtDesc(userId);
            long overdueCount = tasks.stream()
                .filter(t -> !t.isCompleted()
                    && t.getDueDate() != null
                    && t.getDueDate().isBefore(now)
                    && t.getDueDate().isAfter(windowStart))
                .count();

            if (overdueCount > 0) {
                String body = overdueCount == 1
                    ? "1 task just became overdue"
                    : overdueCount + " tasks just became overdue";
                pushService.sendNotification(sub,
                    "Tasks overdue",
                    body,
                    "/",
                    "overdue"
                );
            }
        }
    }

    // ── Daily digest: every day at 8 AM server time ──
    @Scheduled(cron = "0 0 8 * * *")
    public void dailyDigest() {
        Instant now   = Instant.now();
        Instant endOfDay = Instant.now()
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant();

        List<PushSubscription> subs = subRepo.findDailyDigestSubscribers();
        for (PushSubscription sub : subs) {
            Long userId = sub.getUser().getId();
            List<Task> tasks = taskRepo.findByUserIdOrderByCreatedAtDesc(userId);

            long dueToday = tasks.stream()
                .filter(t -> !t.isCompleted()
                    && t.getDueDate() != null
                    && t.getDueDate().isAfter(now)
                    && t.getDueDate().isBefore(endOfDay))
                .count();
            long overdue = tasks.stream()
                .filter(t -> !t.isCompleted()
                    && t.getDueDate() != null
                    && t.getDueDate().isBefore(now))
                .count();
            long noDeadline = tasks.stream()
                .filter(t -> !t.isCompleted() && t.getDueDate() == null)
                .count();

            if (dueToday == 0 && overdue == 0) continue;

            StringBuilder body = new StringBuilder();
            if (dueToday > 0)   body.append(dueToday).append(" due today");
            if (overdue > 0) {
                if (body.length() > 0) body.append(" · ");
                body.append(overdue).append(" overdue");
            }
            if (noDeadline > 0) {
                if (body.length() > 0) body.append(" · ");
                body.append(noDeadline).append(" unscheduled");
            }

            pushService.sendNotification(sub,
                "Good morning! Here's your day",
                body.toString(),
                "/",
                "daily_digest"
            );
        }
    }
}
