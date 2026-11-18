package com.smartcampushelpdesk.helpdesk_api.controllers;

import com.smartcampushelpdesk.helpdesk_api.dto.AdminBookingResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.BookingActionResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.BookingDecisionRequest;
import com.smartcampushelpdesk.helpdesk_api.dto.BookingHistoryResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.BookingRequest;
import com.smartcampushelpdesk.helpdesk_api.dto.BookingResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.PendingBookingResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.ResourceAvailabilityResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.ResourceDirectoryResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.ResourceOptionsResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.ResourceRequest;
import com.smartcampushelpdesk.helpdesk_api.dto.ResourceResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.WeeklyResourceAvailabilityResponse;
import com.smartcampushelpdesk.helpdesk_api.services.ResourceService;
import jakarta.validation.Valid;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

  private final JdbcTemplate jdbcTemplate;
  private final ResourceService resourceService;

  public ResourceController(
    ResourceService resourceService,
    JdbcTemplate jdbcTemplate
  ) {
    this.resourceService = resourceService;
    this.jdbcTemplate = jdbcTemplate;
  }

  @GetMapping
  public List<ResourceDirectoryResponse> getAllResources() {
    return resourceService.getResourceDirectory();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ResourceResponse createResource(
    @Valid @RequestBody ResourceRequest request
  ) {
    return resourceService.createResource(request);
  }

  @PutMapping("/{id}")
  public ResourceResponse updateResource(
    @PathVariable UUID id,
    @Valid @RequestBody ResourceRequest request
  ) {
    return resourceService.updateResource(id, request);
  }

  @PatchMapping("/{id}/status")
  public ResourceResponse updateResourceStatus(
    @PathVariable UUID id,
    @RequestParam Boolean active
  ) {
    return resourceService.updateResourceStatus(id, active);
  }

  @GetMapping("/options")
  public ResourceOptionsResponse getResourceOptions(
    @RequestParam UUID collegeId
  ) {
    var types = jdbcTemplate.query(
      """
      SELECT id, name
      FROM public.resource_types
      WHERE college_id = ?
      ORDER BY name
      """,
      (rs, rowNum) ->
        new ResourceOptionsResponse.ResourceTypeOption(
          rs.getObject("id", UUID.class),
          rs.getString("name")
        ),
      collegeId
    );

    var rooms = jdbcTemplate.query(
      """
      SELECT r.id, r.name, b.name AS building
      FROM public.rooms r
      JOIN public.buildings b ON r.building_id = b.id
      WHERE b.college_id = ?
      ORDER BY b.name, r.name
      """,
      (rs, rowNum) ->
        new ResourceOptionsResponse.RoomOption(
          rs.getObject("id", UUID.class),
          rs.getString("name"),
          rs.getString("building")
        ),
      collegeId
    );

    return new ResourceOptionsResponse(types, rooms);
  }

  @GetMapping("/{id}/availability")
  public ResourceAvailabilityResponse checkAvailability(
    @PathVariable UUID id,
    @RequestParam @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE_TIME
    ) LocalDateTime startTime,
    @RequestParam @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE_TIME
    ) LocalDateTime endTime
  ) {
    return resourceService.checkAvailability(id, startTime, endTime);
  }

  @PostMapping("/bookings")
  @ResponseStatus(HttpStatus.CREATED)
  public BookingResponse createBooking(
    @Valid @RequestBody BookingRequest request,
    @AuthenticationPrincipal Jwt jwt
  ) {
    UUID userId = UUID.fromString(jwt.getSubject());

    return resourceService.createBooking(request, userId);
  }

  @GetMapping("/bookings")
  public List<BookingHistoryResponse> getMyBookings(
    @AuthenticationPrincipal Jwt jwt
  ) {
    UUID userId = UUID.fromString(jwt.getSubject());

    return resourceService.getMyBookings(userId);
  }

  @PatchMapping("/bookings/{bookingId}/cancel")
  public BookingActionResponse cancelBooking(
    @PathVariable UUID bookingId,
    @AuthenticationPrincipal Jwt jwt
  ) {
    UUID userId = UUID.fromString(jwt.getSubject());

    return resourceService.cancelBooking(bookingId, userId);
  }

  @GetMapping("/bookings/pending")
  @PreAuthorize("hasRole('ADMIN')")
  public List<PendingBookingResponse> getPendingBookings(
    @AuthenticationPrincipal Jwt jwt
  ) {
    UUID adminId = UUID.fromString(jwt.getSubject());
    return resourceService.getPendingBookings(adminId);
  }

  @PatchMapping("/bookings/{bookingId}/review")
  @PreAuthorize("hasRole('ADMIN')")
  public BookingActionResponse reviewBooking(
    @PathVariable UUID bookingId,
    @Valid @RequestBody BookingDecisionRequest request,
    @AuthenticationPrincipal Jwt jwt
  ) {
    UUID adminId = UUID.fromString(jwt.getSubject());
    return resourceService.reviewBooking(bookingId, adminId, request);
  }

  @GetMapping("/bookings/all")
  @PreAuthorize("hasRole('ADMIN')")
  public List<AdminBookingResponse> getAllBookings(
    @AuthenticationPrincipal Jwt jwt
  ) {
    UUID adminId = UUID.fromString(jwt.getSubject());
    return resourceService.getAllBookings(adminId);
  }

  @GetMapping("/{id}/availability/week")
  public WeeklyResourceAvailabilityResponse getWeeklyAvailability(
    @PathVariable UUID id,
    @RequestParam @DateTimeFormat(
      iso = DateTimeFormat.ISO.DATE
    ) LocalDate startDate,
    @AuthenticationPrincipal Jwt jwt
  ) {
    UUID userId = UUID.fromString(jwt.getSubject());

    return resourceService.getWeeklyAvailability(id, startDate, userId);
  }
}
