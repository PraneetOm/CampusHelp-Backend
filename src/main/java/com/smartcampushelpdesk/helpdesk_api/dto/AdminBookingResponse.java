package com.smartcampushelpdesk.helpdesk_api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminBookingResponse(
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
        String status,
        LocalDateTime createdAt
) {}