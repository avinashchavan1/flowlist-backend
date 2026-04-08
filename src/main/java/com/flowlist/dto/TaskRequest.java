package com.flowlist.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public class TaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private Instant dueDate;

    private String category;

    private String priority;

    public String getTitle()    { return title; }
    public Instant getDueDate() { return dueDate; }
    public String getCategory() { return category; }
    public String getPriority() { return priority; }
    public void setTitle(String v)    { this.title = v; }
    public void setDueDate(Instant v) { this.dueDate = v; }
    public void setCategory(String v) { this.category = v; }
    public void setPriority(String v) { this.priority = v; }
}
