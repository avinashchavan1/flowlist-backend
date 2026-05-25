package com.flowlist.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "flowlist_push_subscriptions")
public class PushSubscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 600, unique = true)
    private String endpoint;

    @Column(nullable = false, length = 300, name = "p256dh")
    private String p256dh;

    @Column(nullable = false, length = 100)
    private String auth;

    // Notification preferences
    @Column(nullable = false)
    private boolean dueSoon = true;

    @Column(nullable = false)
    private boolean overdueAlerts = true;

    @Column(nullable = false)
    private boolean dailyDigest = true;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // ── Getters / Setters ──────────────────────────────────

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }

    public String getP256dh() { return p256dh; }
    public void setP256dh(String p256dh) { this.p256dh = p256dh; }

    public String getAuth() { return auth; }
    public void setAuth(String auth) { this.auth = auth; }

    public boolean isDueSoon() { return dueSoon; }
    public void setDueSoon(boolean dueSoon) { this.dueSoon = dueSoon; }

    public boolean isOverdueAlerts() { return overdueAlerts; }
    public void setOverdueAlerts(boolean overdueAlerts) { this.overdueAlerts = overdueAlerts; }

    public boolean isDailyDigest() { return dailyDigest; }
    public void setDailyDigest(boolean dailyDigest) { this.dailyDigest = dailyDigest; }

    public Instant getCreatedAt() { return createdAt; }
}
