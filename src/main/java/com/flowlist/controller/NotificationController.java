package com.flowlist.controller;

import com.flowlist.dto.PushSubscriptionRequest;
import com.flowlist.entity.PushSubscription;
import com.flowlist.entity.User;
import com.flowlist.repository.PushSubscriptionRepository;
import com.flowlist.repository.UserRepository;
import com.flowlist.service.PushNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final PushSubscriptionRepository subRepo;
    private final UserRepository userRepo;
    private final PushNotificationService pushService;

    public NotificationController(PushSubscriptionRepository subRepo,
                                   UserRepository userRepo,
                                   PushNotificationService pushService) {
        this.subRepo = subRepo;
        this.userRepo = userRepo;
        this.pushService = pushService;
    }

    // ── Public: return VAPID public key so the browser can subscribe ──
    @GetMapping("/vapid-public-key")
    public ResponseEntity<Map<String, String>> getVapidPublicKey() {
        return ResponseEntity.ok(Map.of("key", pushService.getVapidPublicKey()));
    }

    // ── Save / update a push subscription ──
    @PostMapping("/subscribe")
    public ResponseEntity<?> subscribe(@RequestBody PushSubscriptionRequest req,
                                       @AuthenticationPrincipal UserDetails principal) {
        User user = userRepo.findByEmail(principal.getUsername())
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        Optional<PushSubscription> existing = subRepo.findByEndpoint(req.getEndpoint());
        PushSubscription sub = existing.orElse(new PushSubscription());
        sub.setUser(user);
        sub.setEndpoint(req.getEndpoint());
        sub.setP256dh(req.getP256dh());
        sub.setAuth(req.getAuth());
        sub.setDueSoon(req.isDueSoon());
        sub.setOverdueAlerts(req.isOverdueAlerts());
        sub.setDailyDigest(req.isDailyDigest());

        subRepo.save(sub);
        return ResponseEntity.ok(Map.of("status", "subscribed"));
    }

    // ── Remove a push subscription ──
    @DeleteMapping("/unsubscribe")
    public ResponseEntity<?> unsubscribe(@RequestBody(required = false) Map<String, String> body,
                                         @AuthenticationPrincipal UserDetails principal) {
        User user = userRepo.findByEmail(principal.getUsername())
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        String endpoint = body != null ? body.get("endpoint") : null;
        if (endpoint != null && !endpoint.isBlank()) {
            // Ownership check — only delete if endpoint belongs to this user (prevents IDOR)
            subRepo.findByEndpoint(endpoint).ifPresent(sub -> {
                if (sub.getUser() != null && sub.getUser().getId().equals(user.getId())) {
                    subRepo.deleteByEndpoint(endpoint);
                }
            });
        } else {
            // No endpoint provided — delete all subscriptions for this user
            subRepo.findByUserId(user.getId()).forEach(subRepo::delete);
        }
        return ResponseEntity.ok(Map.of("status", "unsubscribed"));
    }

    // ── Send a test notification to the current user's subscriptions ──
    @PostMapping("/test")
    public ResponseEntity<?> sendTest(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepo.findByEmail(principal.getUsername())
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        var subs = subRepo.findByUserId(user.getId());
        if (subs.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No active subscriptions"));
        }
        int sent = 0;
        for (PushSubscription sub : subs) {
            boolean ok = pushService.sendNotification(sub,
                "Drift notifications work!",
                "You'll get reminders for due tasks, overdue alerts, and daily digests.",
                "/",
                "test"
            );
            if (ok) {
                sent++;
            } else {
                // Invalid subscription — remove it so user can re-subscribe
                subRepo.deleteByEndpoint(sub.getEndpoint());
            }
        }
        if (sent == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "All subscriptions were invalid and have been removed. Please re-enable notifications."));
        }
        return ResponseEntity.ok(Map.of("status", "sent", "count", sent));
    }

    // ── Get current subscription status ──
    @GetMapping("/status")
    public ResponseEntity<?> getStatus(@AuthenticationPrincipal UserDetails principal) {
        User user = userRepo.findByEmail(principal.getUsername())
            .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        var subs = subRepo.findByUserId(user.getId());
        if (subs.isEmpty()) {
            return ResponseEntity.ok(Map.of("subscribed", false));
        }
        PushSubscription sub = subs.get(0);
        return ResponseEntity.ok(Map.of(
            "subscribed",     true,
            "dueSoon",        sub.isDueSoon(),
            "overdueAlerts",  sub.isOverdueAlerts(),
            "dailyDigest",    sub.isDailyDigest()
        ));
    }
}
