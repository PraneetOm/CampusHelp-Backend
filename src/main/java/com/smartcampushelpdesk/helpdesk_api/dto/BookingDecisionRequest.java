package com.smartcampushelpdesk.helpdesk_api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record BookingDecisionRequest(
        @NotNull UUID reviewerId,
        @NotNull Boolean approve,
        String approvalNotes
) {}