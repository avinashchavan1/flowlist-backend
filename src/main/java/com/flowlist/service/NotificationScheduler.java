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
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);
    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");
    private static final DateTimeFormatter TIME_FMT =
        DateTimeFormatter.ofPattern("h:mm a").withZone(IST);

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

    // ── Due Soon: every minute, check for tasks due in 29–31 minutes ──
    @Scheduled(fixedRate = 60_000)
    public void checkDueSoon() {
        Instant windowStart = Instant.now().plusSeconds(29 * 60);
        Instant windowEnd   = Instant.now().plusSeconds(31 * 60);

        List<PushSubscription> subs = subRepo.findDueSoonSubscribers();
        for (PushSubscription sub : subs) {
            Long userId = sub.getUser().getId();
            List<Task> tasks = taskRepo.findByUserIdAndCompletedFalse(userId);
            for (Task task : tasks) {
                if (task.isCompleted() || task.getDueDate() == null) continue;
                Instant due = task.getDueDate();
                if (due.isAfter(windowStart) && due.isBefore(windowEnd)) {
                    String timeStr = TIME_FMT.format(due);
                    boolean ok = pushService.sendNotification(sub,
                        "Due in 30 minutes",
                        task.getTitle() + " is due at " + timeStr,
                        "/?task=" + task.getId(),
                        "due_soon"
                    );
                    if (!ok) { subRepo.deleteByEndpoint(sub.getEndpoint()); break; }
                }
            }
        }
    }

    // ── Overdue: every hour, alert for ALL currently overdue tasks ──
    @Scheduled(fixedRate = 3_600_000)
    public void checkOverdue() {
        Instant now = Instant.now();

        List<PushSubscription> subs = subRepo.findOverdueSubscribers();
        for (PushSubscription sub : subs) {
            Long userId = sub.getUser().getId();
            List<Task> tasks = taskRepo.findByUserIdAndCompletedFalse(userId);
            long overdueCount = tasks.stream()
                .filter(t -> !t.isCompleted()
                    && t.getDueDate() != null
                    && t.getDueDate().isBefore(now))
                .count();

            if (overdueCount > 0) {
                String body = overdueCount == 1
                    ? "1 task is overdue"
                    : overdueCount + " tasks are overdue";
                boolean ok = pushService.sendNotification(sub,
                    "Overdue tasks",
                    body,
                    "/",
                    "overdue"
                );
                if (!ok) subRepo.deleteByEndpoint(sub.getEndpoint());
            }
        }
    }

    // ── Weekly summary: every Sunday at 6 PM IST (12:30 UTC) ──
    @Scheduled(cron = "0 30 12 * * SUN")
    public void weeklySummary() {
        Instant weekAgo = Instant.now().minus(7, ChronoUnit.DAYS);

        List<PushSubscription> subs = subRepo.findDailyDigestSubscribers();
        for (PushSubscription sub : subs) {
            Long userId = sub.getUser().getId();
            // Weekly summary needs both completed (within last 7d) AND pending — pull all tasks
            List<Task> tasks = taskRepo.findByUserIdOrderByCreatedAtDesc(userId);

            long completed = tasks.stream()
                .filter(t -> t.isCompleted()
                    && t.getCompletedAt() != null
                    && t.getCompletedAt().isAfter(weekAgo))
                .count();

            if (completed == 0) continue;

            long pending = tasks.stream().filter(t -> !t.isCompleted()).count();

            String body = completed + " task" + (completed != 1 ? "s" : "") + " completed this week";
            if (pending > 0) body += " · " + pending + " still pending";

            boolean ok = pushService.sendNotification(sub,
                "Your week in review",
                body,
                "/",
                "weekly_summary"
            );
            if (!ok) subRepo.deleteByEndpoint(sub.getEndpoint());
        }
    }

    // ── Daily digest: every day at 8 AM IST (2:30 UTC) ──
    @Scheduled(cron = "0 30 2 * * *")
    public void dailyDigest() {
        Instant now   = Instant.now();
        Instant endOfDay = Instant.now()
            .atZone(IST)
            .toLocalDate()
            .atTime(23, 59, 59)
            .atZone(IST)
            .toInstant();

        List<PushSubscription> subs = subRepo.findDailyDigestSubscribers();
        for (PushSubscription sub : subs) {
            Long userId = sub.getUser().getId();
            List<Task> tasks = taskRepo.findByUserIdAndCompletedFalse(userId);

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

            // Always send digest if there are any pending tasks
            if (dueToday == 0 && overdue == 0 && noDeadline == 0) continue;

            StringBuilder body = new StringBuilder();
            if (overdue > 0)    body.append(overdue).append(" overdue");
            if (dueToday > 0) {
                if (body.length() > 0) body.append(" · ");
                body.append(dueToday).append(" due today");
            }
            if (noDeadline > 0) {
                if (body.length() > 0) body.append(" · ");
                body.append(noDeadline).append(" unscheduled");
            }

            boolean ok = pushService.sendNotification(sub,
                "Good morning! Here's your day",
                body.toString(),
                "/",
                "daily_digest"
            );
            if (!ok) subRepo.deleteByEndpoint(sub.getEndpoint());
        }
    }
}
