package com.smartcampushelpdesk.helpdesk_api.dto;

import java.util.UUID;

public record ResourceDirectoryResponse(
        UUID id,
        UUID resourceTypeId,
        UUID roomId,
        String name,
        String code,
        String description,
        String type,
        String room,
        String building,
        Integer capacity,
        Boolean bookable,
        Boolean active
) {}