package com.smartcampushelpdesk.helpdesk_api.dto;

import jakarta.validation.constraints.NotNull;

public record BookingDecisionRequest(
    @NotNull Boolean approve,
    String approvalNotes
) {}