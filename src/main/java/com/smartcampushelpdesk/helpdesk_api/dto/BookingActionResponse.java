package com.smartcampushelpdesk.helpdesk_api.dto;

import java.util.UUID;

public record BookingActionResponse(
        UUID bookingId,
        String status,
        String message
) {}