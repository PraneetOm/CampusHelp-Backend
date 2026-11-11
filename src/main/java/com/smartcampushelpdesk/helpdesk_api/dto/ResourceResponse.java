package com.smartcampushelpdesk.helpdesk_api.dto;

import java.util.UUID;

public record ResourceResponse(
        UUID id,
        UUID collegeId,
        UUID resourceTypeId,
        UUID roomId,
        String name,
        String code,
        String description,
        Integer capacity,
        Boolean bookable,
        Boolean active
) {
}