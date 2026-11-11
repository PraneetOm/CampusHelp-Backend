package com.smartcampushelpdesk.helpdesk_api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingHistoryResponse(
        UUID id,
        UUID resourceId,
        String resourceName,
        String purpose,
        Integer attendeeCount,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status,
        LocalDateTime createdAt
) {}