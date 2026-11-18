package com.smartcampushelpdesk.helpdesk_api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record BookingRequest(

    @NotNull UUID resourceId,

    @NotBlank String purpose,

    @NotNull
    @Min(1)
    Integer attendeeCount,

    @NotNull LocalDateTime startTime,

    @NotNull LocalDateTime endTime

) {}