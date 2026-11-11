package com.smartcampushelpdesk.helpdesk_api.dto;

import java.util.UUID;

public record ResourceAvailabilityResponse(
        UUID resourceId,
        boolean available,
        String message
) {}