package com.flowlist.dto;

import com.flowlist.entity.Task;
import java.time.Instant;

public class TaskResponse {
    private String id;
    private String title;
    private String notes;
    private Instant dueDate;
    private String category;
    private String priority;
    private String recurrence;
    private boolean completed;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static TaskResponse from(Task t) {
        TaskResponse r = new TaskResponse();
        r.id          = t.getId();
        r.title       = t.getTitle();
        r.notes       = t.getNotes();
        r.dueDate     = t.getDueDate();
        r.category    = t.getCategory();
        r.priority    = t.getPriority();
        r.recurrence  = t.getRecurrence();
        r.completed   = t.isCompleted();
        r.completedAt = t.getCompletedAt();
        r.createdAt   = t.getCreatedAt();
        r.updatedAt   = t.getUpdatedAt();
        return r;
    }

    public String getId()             { return id; }
    public String getTitle()          { return title; }
    public String getNotes()          { return notes; }
    public Instant getDueDate()       { return dueDate; }
    public String getCategory()       { return category; }
    public String getPriority()       { return priority; }
    public String getRecurrence()     { return recurrence; }
    public boolean isCompleted()      { return completed; }
    public Instant getCompletedAt()   { return completedAt; }
    public Instant getCreatedAt()     { return createdAt; }
    public Instant getUpdatedAt()     { return updatedAt; }
}
