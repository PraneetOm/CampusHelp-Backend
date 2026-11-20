package com.smartcampushelpdesk.helpdesk_api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class TicketAssignmentRequest {

    @NotNull(message = "Staff ID is required")
    private UUID assignedTo;

    public UUID getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(UUID assignedTo) {
        this.assignedTo = assignedTo;
    }
}