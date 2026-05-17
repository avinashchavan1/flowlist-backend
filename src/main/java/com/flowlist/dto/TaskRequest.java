package com.flowlist.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public class TaskRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String notes;

    private Instant dueDate;

    private String category;

    private String priority;

    private String recurrence;

    /** Series start — first occurrence date+time. Triggers bulk creation when set alongside recurrenceEnd. */
    private Instant recurrenceStart;

    /** Series end — last date up to which occurrences are generated. Not stored on individual tasks. */
    private Instant recurrenceEnd;

    public String getTitle()              { return title; }
    public String getNotes()              { return notes; }
    public Instant getDueDate()           { return dueDate; }
    public String getCategory()           { return category; }
    public String getPriority()           { return priority; }
    public String getRecurrence()         { return recurrence; }
    public Instant getRecurrenceStart()   { return recurrenceStart; }
    public Instant getRecurrenceEnd()     { return recurrenceEnd; }

    public void setTitle(String v)             { this.title = v; }
    public void setNotes(String v)             { this.notes = v; }
    public void setDueDate(Instant v)          { this.dueDate = v; }
    public void setCategory(String v)          { this.category = v; }
    public void setPriority(String v)          { this.priority = v; }
    public void setRecurrence(String v)        { this.recurrence = v; }
    public void setRecurrenceStart(Instant v)  { this.recurrenceStart = v; }
    public void setRecurrenceEnd(Instant v)    { this.recurrenceEnd = v; }
}
