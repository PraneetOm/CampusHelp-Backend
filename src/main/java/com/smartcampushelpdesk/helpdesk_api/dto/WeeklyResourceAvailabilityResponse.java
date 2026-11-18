package com.smartcampushelpdesk.helpdesk_api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public record WeeklyResourceAvailabilityResponse(
    UUID resourceId,
    LocalDate startDate,
    List<DayAvailability> days
) {
    public record DayAvailability(
        LocalDate date,
        List<SlotAvailability> slots
    ) {}

    public record SlotAvailability(
        LocalTime startTime,
        LocalTime endTime,
        boolean available
    ) {}
}