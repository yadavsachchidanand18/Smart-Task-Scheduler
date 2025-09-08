package com.example.taskscheduler.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Task {
    private String id;
    private String title;
    private String description;
    private Priority priority;
    private LocalDateTime deadline; // may be null
    private LocalDateTime createdAt;
    private boolean completed;
    private Integer reminderMinutesBefore; // nullable; if present, schedule reminder

    public Task() {
        this.id = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.priority = Priority.MEDIUM;
        this.completed = false;
    }

    public Task(String title, String description, Priority priority,
                LocalDateTime deadline, Integer reminderMinutesBefore) {
        this();
        this.title = title;
        this.description = description;
        this.priority = priority == null ? Priority.MEDIUM : priority;
        this.deadline = deadline;
        this.reminderMinutesBefore = reminderMinutesBefore;
    }

    // Getters and setters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Priority getPriority() { return priority; }
    public LocalDateTime getDeadline() { return deadline; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isCompleted() { return completed; }
    public Integer getReminderMinutesBefore() { return reminderMinutesBefore; }

    public void setId(String id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setCompleted(boolean completed) { this.completed = completed; }
    public void setReminderMinutesBefore(Integer reminderMinutesBefore) { this.reminderMinutesBefore = reminderMinutesBefore; }

    // Urgency comparator helper
    public int urgencyCompareTo(Task other) {
        // Higher priority first
        int p = Integer.compare(other.getPriority().weight(), this.getPriority().weight());
        if (p != 0) return p;
        // Earlier deadline first; nulls last
        if (this.getDeadline() == null && other.getDeadline() != null) return 1;
        if (this.getDeadline() != null && other.getDeadline() == null) return -1;
        if (this.getDeadline() != null && other.getDeadline() != null) {
            int d = this.getDeadline().compareTo(other.getDeadline());
            if (d != 0) return d;
        }
        // Earlier created first
        return this.getCreatedAt().compareTo(other.getCreatedAt());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }

    @Override
    public String toString() {
        return "[" + priority + "] " + (completed ? "(Done) " : "") + title;
    }
}
