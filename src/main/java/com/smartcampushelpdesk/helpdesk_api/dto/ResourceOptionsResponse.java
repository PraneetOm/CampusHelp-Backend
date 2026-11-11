package com.smartcampushelpdesk.helpdesk_api.dto;

import java.util.List;
import java.util.UUID;

public record ResourceOptionsResponse(
        List<ResourceTypeOption> resourceTypes,
        List<RoomOption> rooms
) {
    public record ResourceTypeOption(
            UUID id,
            String name
    ) {}

    public record RoomOption(
            UUID id,
            String name,
            String building
    ) {}
}