package com.smartcampushelpdesk.helpdesk_api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record ResourceRequest(
        @NotNull UUID collegeId,
        UUID resourceTypeId,
        UUID roomId,

        @NotBlank String name,
        String code,
        String description,

        @Positive Integer capacity,

        Boolean bookable,
        Boolean active
) {}