package com.smartcampushelpdesk.helpdesk_api.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
        UUID id,
        UUID collegeId,
        UUID resourceId,
        UUID bookedBy,
        String purpose,
        Integer attendeeCount,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String status
) {}