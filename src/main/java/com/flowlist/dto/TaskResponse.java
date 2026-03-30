package com.flowlist.dto;

import com.flowlist.entity.Task;
import java.time.Instant;

public class TaskResponse {
    private String id;
    private String title;
    private Instant dueDate;
    private String category;
    private boolean completed;
    private Instant completedAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static TaskResponse from(Task t) {
        TaskResponse r = new TaskResponse();
        r.id          = t.getId();
        r.title       = t.getTitle();
        r.dueDate     = t.getDueDate();
        r.category    = t.getCategory();
        r.completed   = t.isCompleted();
        r.completedAt = t.getCompletedAt();
        r.createdAt   = t.getCreatedAt();
        r.updatedAt   = t.getUpdatedAt();
        return r;
    }

    public String getId()           { return id; }
    public String getTitle()        { return title; }
    public Instant getDueDate()     { return dueDate; }
    public String getCategory()     { return category; }
    public boolean isCompleted()    { return completed; }
    public Instant getCompletedAt() { return completedAt; }
    public Instant getCreatedAt()   { return createdAt; }
    public Instant getUpdatedAt()   { return updatedAt; }
}
