package com.smartcampushelpdesk.helpdesk_api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record PendingBookingResponse(
        UUID id,
        UUID resourceId,
        String resourceName,
        UUID bookedBy,
        String requesterName,
        String requesterEmail,
        String purpose,
        Integer attendeeCount,
        LocalDateTime startTime,
        LocalDateTime endTime,
        LocalDateTime createdAt
) {}