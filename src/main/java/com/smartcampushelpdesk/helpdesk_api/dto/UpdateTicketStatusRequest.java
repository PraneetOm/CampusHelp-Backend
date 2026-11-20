
package com.smartcampushelpdesk.helpdesk_api.dto;

import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public class UpdateTicketStatusRequest {

    @NotNull(message = "Status ID is required")
    private UUID statusId;

    public UUID getStatusId() {
        return statusId;
    }

    public void setStatusId(UUID statusId) {
        this.statusId = statusId;
    }
}
