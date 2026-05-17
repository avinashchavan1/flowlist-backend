package com.flowlist.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_task_user",                columnList = "user_id"),
    @Index(name = "idx_task_due",                 columnList = "due_date"),
    // Composite index covers scheduler queries: per-user filter on completed + due_date range
    @Index(name = "idx_task_user_completed_due",  columnList = "user_id, completed, due_date")
})
public class Task {

    @Id
    @Column(length = 36)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String title;

    @Column(length = 2000)
    private String notes;

    @Column(name = "due_date")
    private Instant dueDate;

    @Column(length = 100)
    private String category;

    @Column(length = 20)
    private String priority;

    @Column(length = 20)
    private String recurrence;

    /**
     * Set on every task that belongs to a pre-created recurring series.
     * Null  → old-style single task (auto-spawns next occurrence on complete).
     * Non-null → series task (all occurrences already exist; do NOT auto-spawn).
     */
    @Column(name = "recurrence_start")
    private Instant recurrenceStart;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    void prePersist() { createdAt = updatedAt = Instant.now(); }

    @PreUpdate
    void preUpdate()  { updatedAt = Instant.now(); }

    public Task() {}

    public String getId()               { return id; }
    public User getUser()               { return user; }
    public String getTitle()            { return title; }
    public String getNotes()            { return notes; }
    public Instant getDueDate()         { return dueDate; }
    public String getCategory()         { return category; }
    public String getPriority()         { return priority; }
    public String getRecurrence()       { return recurrence; }
    public Instant getRecurrenceStart() { return recurrenceStart; }
    public boolean isCompleted()        { return completed; }
    public Instant getCompletedAt()     { return completedAt; }
    public Instant getCreatedAt()       { return createdAt; }
    public Instant getUpdatedAt()       { return updatedAt; }

    public void setId(String id)                   { this.id = id; }
    public void setUser(User user)                 { this.user = user; }
    public void setTitle(String title)             { this.title = title; }
    public void setNotes(String notes)             { this.notes = notes; }
    public void setDueDate(Instant d)              { this.dueDate = d; }
    public void setCategory(String c)              { this.category = c; }
    public void setPriority(String p)              { this.priority = p; }
    public void setRecurrence(String r)            { this.recurrence = r; }
    public void setRecurrenceStart(Instant t)      { this.recurrenceStart = t; }
    public void setCompleted(boolean b)            { this.completed = b; }
    public void setCompletedAt(Instant t)          { this.completedAt = t; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final Task t = new Task();
        public Builder id(String v)                 { t.id = v;               return this; }
        public Builder user(User v)                 { t.user = v;             return this; }
        public Builder title(String v)              { t.title = v;            return this; }
        public Builder notes(String v)              { t.notes = v;            return this; }
        public Builder dueDate(Instant v)           { t.dueDate = v;          return this; }
        public Builder category(String v)           { t.category = v;         return this; }
        public Builder priority(String v)           { t.priority = v;         return this; }
        public Builder recurrence(String v)         { t.recurrence = v;       return this; }
        public Builder recurrenceStart(Instant v)   { t.recurrenceStart = v;  return this; }
        public Builder completed(boolean v)         { t.completed = v;        return this; }
        public Task build() { return t; }
    }
}
