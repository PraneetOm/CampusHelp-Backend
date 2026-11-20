package com.smartcampushelpdesk.helpdesk_api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateTicketRequest {

    @NotBlank(message = "Ticket title is required")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    private String title;

    @NotBlank(message = "Ticket description is required")
    @Size(max = 5000, message = "Description cannot exceed 5000 characters")
    private String description;

    private UUID categoryId;

    @Size(max = 500, message = "Location cannot exceed 500 characters")
    private String location;

    @Pattern(
        regexp = "(?i)^(LOW|MEDIUM|HIGH)$",
        message = "Priority must be LOW, MEDIUM or HIGH"
    )
    private String priority;

    private LocalDateTime dueDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }
}