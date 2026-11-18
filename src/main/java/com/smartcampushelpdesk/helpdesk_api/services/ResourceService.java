package com.smartcampushelpdesk.helpdesk_api.services;

import com.smartcampushelpdesk.helpdesk_api.dto.AdminBookingResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.BookingActionResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.BookingDecisionRequest;
import com.smartcampushelpdesk.helpdesk_api.dto.BookingHistoryResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.BookingRequest;
import com.smartcampushelpdesk.helpdesk_api.dto.BookingResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.PendingBookingResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.ResourceAvailabilityResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.ResourceDirectoryResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.ResourceRequest;
import com.smartcampushelpdesk.helpdesk_api.dto.ResourceResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.WeeklyResourceAvailabilityResponse;
import com.smartcampushelpdesk.helpdesk_api.entities.Resource;
import com.smartcampushelpdesk.helpdesk_api.repositories.ResourceRepository;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ResourceService {

  private final JdbcTemplate jdbcTemplate;
  private final ResourceRepository resourceRepository;

  public ResourceService(
    ResourceRepository resourceRepository,
    JdbcTemplate jdbcTemplate
  ) {
    this.resourceRepository = resourceRepository;
    this.jdbcTemplate = jdbcTemplate;
  }

  public List<ResourceResponse> getAllResources() {
    List<Resource> resources = resourceRepository.findAll();

    return resources
      .stream()
      .map(resource ->
        new ResourceResponse(
          resource.getId(),
          resource.getCollegeId(),
          resource.getResourceTypeId(),
          resource.getRoomId(),
          resource.getName(),
          resource.getCode(),
          resource.getDescription(),
          resource.getCapacity(),
          resource.getBookable(),
          resource.getActive()
        )
      )
      .toList();
  }

  public List<ResourceDirectoryResponse> getResourceDirectory() {
    return resourceRepository
      .findResourceDirectory()
      .stream()
      .map(row ->
        new ResourceDirectoryResponse(
          (java.util.UUID) row[0],
          (java.util.UUID) row[1],
          (java.util.UUID) row[2],
          (String) row[3],
          (String) row[4],
          (String) row[5],
          (String) row[6],
          (String) row[7],
          (String) row[8],
          row[9] == null ? null : ((Number) row[9]).intValue(),
          (Boolean) row[10],
          (Boolean) row[11]
        )
      )
      .toList();
  }

  public ResourceResponse createResource(ResourceRequest request) {
    Resource resource = new Resource();

    resource.setCollegeId(request.collegeId());
    resource.setResourceTypeId(request.resourceTypeId());
    resource.setRoomId(request.roomId());

    resource.setName(request.name());
    resource.setCode(request.code());
    resource.setDescription(request.description());
    resource.setCapacity(request.capacity());

    resource.setBookable(
      request.bookable() != null ? request.bookable() : true
    );

    resource.setActive(request.active() != null ? request.active() : true);

    Resource saved = resourceRepository.save(resource);

    return new ResourceResponse(
      saved.getId(),
      saved.getCollegeId(),
      saved.getResourceTypeId(),
      saved.getRoomId(),
      saved.getName(),
      saved.getCode(),
      saved.getDescription(),
      saved.getCapacity(),
      saved.getBookable(),
      saved.getActive()
    );
  }

  public ResourceResponse updateResource(UUID id, ResourceRequest request) {
    Resource resource = resourceRepository
      .findById(id)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")
      );

    resource.setCollegeId(request.collegeId());
    resource.setResourceTypeId(request.resourceTypeId());
    resource.setRoomId(request.roomId());

    resource.setName(request.name());
    resource.setCode(request.code());
    resource.setDescription(request.description());
    resource.setCapacity(request.capacity());

    if (request.bookable() != null) {
      resource.setBookable(request.bookable());
    }

    if (request.active() != null) {
      resource.setActive(request.active());
    }

    Resource updated = resourceRepository.save(resource);

    return toResourceResponse(updated);
  }

  public ResourceResponse updateResourceStatus(UUID id, Boolean active) {
    Resource resource = resourceRepository
      .findById(id)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")
      );

    resource.setActive(active);

    Resource updated = resourceRepository.save(resource);

    return toResourceResponse(updated);
  }

  private ResourceResponse toResourceResponse(Resource resource) {
    return new ResourceResponse(
      resource.getId(),
      resource.getCollegeId(),
      resource.getResourceTypeId(),
      resource.getRoomId(),
      resource.getName(),
      resource.getCode(),
      resource.getDescription(),
      resource.getCapacity(),
      resource.getBookable(),
      resource.getActive()
    );
  }

  public ResourceAvailabilityResponse checkAvailability(
    UUID resourceId,
    LocalDateTime startTime,
    LocalDateTime endTime
  ) {
    if (startTime == null || endTime == null || !endTime.isAfter(startTime)) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "End time must be later than start time"
      );
    }

    Resource resource = resourceRepository
      .findById(resourceId)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")
      );

    if (
      !Boolean.TRUE.equals(resource.getActive()) ||
      !Boolean.TRUE.equals(resource.getBookable())
    ) {
      return new ResourceAvailabilityResponse(
        resourceId,
        false,
        "This resource is inactive or not bookable"
      );
    }

    String bookingSql = """
      SELECT COUNT(*)
      FROM public.bookings
      WHERE resource_id = ?
      AND status IN ('PENDING', 'CONFIRMED')
      AND start_time < ?
      AND end_time > ?
      """;

    Integer bookingConflicts = jdbcTemplate.queryForObject(
      bookingSql,
      Integer.class,
      resourceId,
      endTime,
      startTime
    );

    String blockSql = """
      SELECT COUNT(*)
      FROM public.booking_blocks
      WHERE resource_id = ?
      AND start_time < ?
      AND end_time > ?
      """;

    Integer blockConflicts = jdbcTemplate.queryForObject(
      blockSql,
      Integer.class,
      resourceId,
      endTime,
      startTime
    );

    boolean available = bookingConflicts == 0 && blockConflicts == 0;

    return new ResourceAvailabilityResponse(
      resourceId,
      available,
      available
        ? "Resource is available for the selected time"
        : "Resource is already booked or blocked during this time"
    );
  }

  @Transactional
  public BookingResponse createBooking(BookingRequest request, UUID userId) {
    if (!request.endTime().isAfter(request.startTime())) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "End time must be later than start time"
      );
    }

    if (request.attendeeCount() == null || request.attendeeCount() < 1) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "At least one attendee is required"
      );
    }

    // Identity and college come from the authenticated user.
    UUID collegeId = getUserCollegeId(userId);

    Resource resource = resourceRepository
      .findById(request.resourceId())
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")
      );

    // Prevent booking resources belonging to another college.
    if (!collegeId.equals(resource.getCollegeId())) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "You cannot book resources belonging to another college"
      );
    }

    if (
      resource.getCapacity() != null &&
      request.attendeeCount() > resource.getCapacity()
    ) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Attendee count exceeds resource capacity of " + resource.getCapacity()
      );
    }

    ResourceAvailabilityResponse availability = checkAvailability(
      request.resourceId(),
      request.startTime(),
      request.endTime()
    );

    if (!availability.available()) {
      throw new ResponseStatusException(
        HttpStatus.CONFLICT,
        availability.message()
      );
    }

    String sql = """
      INSERT INTO public.bookings (
          college_id,
          resource_id,
          booked_by,
          purpose,
          attendee_count,
          start_time,
          end_time,
          status
      )
      VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING')
      RETURNING id
      """;

    UUID bookingId = jdbcTemplate.queryForObject(
      sql,
      UUID.class,
      collegeId,
      request.resourceId(),
      userId,
      request.purpose(),
      request.attendeeCount(),
      Timestamp.valueOf(request.startTime()),
      Timestamp.valueOf(request.endTime())
    );

    return new BookingResponse(
      bookingId,
      collegeId,
      request.resourceId(),
      userId,
      request.purpose(),
      request.attendeeCount(),
      request.startTime(),
      request.endTime(),
      "PENDING"
    );
  }

  public List<BookingHistoryResponse> getMyBookings(UUID userId) {
    String sql = """
            SELECT
            b.id,
            b.resource_id,
            r.name AS resource_name,
            b.purpose,
      b.attendee_count,
      b.start_time,
            b.end_time,
            b.status,
            b.created_at
            FROM public.bookings b
            JOIN public.resources r
            ON b.resource_id = r.id
            WHERE b.booked_by = ?
            ORDER BY b.created_at DESC
            """;

    return jdbcTemplate.query(
      sql,
      (rs, rowNum) ->
        new BookingHistoryResponse(
          rs.getObject("id", UUID.class),
          rs.getObject("resource_id", UUID.class),
          rs.getString("resource_name"),
          rs.getString("purpose"),
          rs.getInt("attendee_count"),
          rs.getTimestamp("start_time").toLocalDateTime(),
          rs.getTimestamp("end_time").toLocalDateTime(),
          rs.getString("status"),
          rs.getTimestamp("created_at").toLocalDateTime()
        ),
      userId
    );
  }

  @Transactional
  public BookingActionResponse cancelBooking(UUID bookingId, UUID userId) {
    String sql = """
      UPDATE public.bookings
      SET status = 'CANCELLED',
      updated_at = CURRENT_TIMESTAMP
      WHERE id = ?
      AND booked_by = ?
      AND status IN ('PENDING', 'CONFIRMED')
      """;

    int updated = jdbcTemplate.update(sql, bookingId, userId);

    if (updated == 0) {
      throw new ResponseStatusException(
        HttpStatus.CONFLICT,
        "Booking not found, not owned by you, or cannot be cancelled"
      );
    }

    return new BookingActionResponse(
      bookingId,
      "CANCELLED",
      "Booking cancelled successfully"
    );
  }

  public List<PendingBookingResponse> getPendingBookings(UUID adminId) {
    UUID collegeId = getUserCollegeId(adminId);

    String sql = """
      SELECT
          b.id,
          b.resource_id,
          r.name AS resource_name,
          b.booked_by,
          u.name AS requester_name,
          u.email AS requester_email,
          b.purpose,
          b.attendee_count,
          b.start_time,
          b.end_time,
          b.created_at
      FROM public.bookings b
      JOIN public.resources r ON r.id = b.resource_id
      JOIN public.users u ON u.id = b.booked_by
      WHERE b.college_id = ?
        AND b.status = 'PENDING'
      ORDER BY b.created_at ASC
      """;

    return jdbcTemplate.query(
      sql,
      (rs, rowNum) ->
        new PendingBookingResponse(
          rs.getObject("id", UUID.class),
          rs.getObject("resource_id", UUID.class),
          rs.getString("resource_name"),
          rs.getObject("booked_by", UUID.class),
          rs.getString("requester_name"),
          rs.getString("requester_email"),
          rs.getString("purpose"),
          rs.getInt("attendee_count"),
          rs.getTimestamp("start_time").toLocalDateTime(),
          rs.getTimestamp("end_time").toLocalDateTime(),
          rs.getTimestamp("created_at").toLocalDateTime()
        ),
      collegeId
    );
  }

  public List<AdminBookingResponse> getAllBookings(UUID adminId) {
    UUID collegeId = getUserCollegeId(adminId);

    String sql = """
      SELECT
          b.id,
          b.resource_id,
          r.name AS resource_name,
          b.booked_by,
          u.name AS requester_name,
          u.email AS requester_email,
          b.purpose,
          b.attendee_count,
          b.start_time,
          b.end_time,
          b.status,
          b.created_at
      FROM public.bookings b
      JOIN public.resources r ON r.id = b.resource_id
      JOIN public.users u ON u.id = b.booked_by
      WHERE b.college_id = ?
      ORDER BY b.created_at DESC
      """;

    return jdbcTemplate.query(
      sql,
      (rs, rowNum) ->
        new AdminBookingResponse(
          rs.getObject("id", UUID.class),
          rs.getObject("resource_id", UUID.class),
          rs.getString("resource_name"),
          rs.getObject("booked_by", UUID.class),
          rs.getString("requester_name"),
          rs.getString("requester_email"),
          rs.getString("purpose"),
          rs.getInt("attendee_count"),
          rs.getTimestamp("start_time").toLocalDateTime(),
          rs.getTimestamp("end_time").toLocalDateTime(),
          rs.getString("status"),
          rs.getTimestamp("created_at").toLocalDateTime()
        ),
      collegeId
    );
  }

  @Transactional
  public BookingActionResponse reviewBooking(
    UUID bookingId,
    UUID adminId,
    BookingDecisionRequest request
  ) {
    UUID adminCollegeId = getUserCollegeId(adminId);

    String findSql = """
      SELECT college_id, resource_id, start_time, end_time, status
      FROM public.bookings
      WHERE id = ?
      """;

    var bookings = jdbcTemplate.query(
      findSql,
      (rs, rowNum) ->
        new Object[] {
          rs.getObject("college_id", UUID.class),
          rs.getObject("resource_id", UUID.class),
          rs.getTimestamp("start_time").toLocalDateTime(),
          rs.getTimestamp("end_time").toLocalDateTime(),
          rs.getString("status"),
        },
      bookingId
    );

    if (bookings.isEmpty()) {
      throw new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "Booking not found"
      );
    }

    Object[] booking = bookings.get(0);

    UUID bookingCollegeId = (UUID) booking[0];
    UUID resourceId = (UUID) booking[1];
    LocalDateTime startTime = (LocalDateTime) booking[2];
    LocalDateTime endTime = (LocalDateTime) booking[3];
    String currentStatus = (String) booking[4];

    if (!adminCollegeId.equals(bookingCollegeId)) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "You cannot review bookings belonging to another college"
      );
    }

    if (!"PENDING".equals(currentStatus)) {
      throw new ResponseStatusException(
        HttpStatus.CONFLICT,
        "Only pending bookings can be reviewed"
      );
    }

    boolean approving = Boolean.TRUE.equals(request.approve());
    String newStatus = approving ? "CONFIRMED" : "REJECTED";

    if (approving) {
      Resource resource = resourceRepository
        .findById(resourceId)
        .orElseThrow(() ->
          new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Resource not found"
          )
        );

      if (
        !Boolean.TRUE.equals(resource.getActive()) ||
        !Boolean.TRUE.equals(resource.getBookable())
      ) {
        throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Resource is inactive or not bookable"
        );
      }

      if (!adminCollegeId.equals(resource.getCollegeId())) {
        throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "Resource does not belong to your college"
        );
      }

      String conflictSql = """
        SELECT COUNT(*)
        FROM public.bookings
        WHERE resource_id = ?
          AND id <> ?
          AND status IN ('PENDING', 'CONFIRMED')
          AND start_time < ?
          AND end_time > ?
        """;

      Integer conflicts = jdbcTemplate.queryForObject(
        conflictSql,
        Integer.class,
        resourceId,
        bookingId,
        Timestamp.valueOf(endTime),
        Timestamp.valueOf(startTime)
      );

      String blockSql = """
        SELECT COUNT(*)
        FROM public.booking_blocks
        WHERE resource_id = ?
          AND start_time < ?
          AND end_time > ?
        """;

      Integer blocks = jdbcTemplate.queryForObject(
        blockSql,
        Integer.class,
        resourceId,
        Timestamp.valueOf(endTime),
        Timestamp.valueOf(startTime)
      );

      if (conflicts > 0 || blocks > 0) {
        throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Resource is no longer available for this time slot"
        );
      }
    }

    String updateSql = """
      UPDATE public.bookings
      SET status = ?,
          approved_by = ?,
          approval_notes = ?,
          updated_at = CURRENT_TIMESTAMP
      WHERE id = ?
        AND college_id = ?
        AND status = 'PENDING'
      """;

    int updated = jdbcTemplate.update(
      updateSql,
      newStatus,
      adminId,
      request.approvalNotes(),
      bookingId,
      adminCollegeId
    );

    if (updated == 0) {
      throw new ResponseStatusException(
        HttpStatus.CONFLICT,
        "Booking has already been reviewed or is no longer available"
      );
    }

    return new BookingActionResponse(
      bookingId,
      newStatus,
      approving
        ? "Booking approved successfully"
        : "Booking rejected successfully"
    );
  }

  private UUID getUserCollegeId(UUID userId) {
    try {
      return jdbcTemplate.queryForObject(
        """
        SELECT college_id
        FROM public.users
        WHERE id = ?
        AND status = 'ACTIVE'
        """,
        UUID.class,
        userId
      );
    } catch (org.springframework.dao.EmptyResultDataAccessException ex) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Active campus profile not found"
      );
    }
  }

  public WeeklyResourceAvailabilityResponse getWeeklyAvailability(
    UUID resourceId,
    LocalDate startDate,
    UUID userId
  ) {
    if (startDate == null) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Start date is required"
      );
    }

    UUID collegeId = getUserCollegeId(userId);

    Resource resource = resourceRepository
      .findById(resourceId)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Resource not found")
      );

    if (!collegeId.equals(resource.getCollegeId())) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "You cannot view resources belonging to another college"
      );
    }

    LocalDateTime rangeStart = startDate.atTime(9, 0);
    LocalDateTime rangeEnd = startDate.plusDays(7).atTime(17, 0);

    String bookingSql = """
      SELECT start_time, end_time
      FROM public.bookings
      WHERE resource_id = ?
        AND status IN ('PENDING', 'CONFIRMED')
        AND start_time < ?
        AND end_time > ?
      """;

    List<TimeRange> bookings = jdbcTemplate.query(
      bookingSql,
      (rs, rowNum) ->
        new TimeRange(
          rs.getTimestamp("start_time").toLocalDateTime(),
          rs.getTimestamp("end_time").toLocalDateTime()
        ),
      resourceId,
      Timestamp.valueOf(rangeEnd),
      Timestamp.valueOf(rangeStart)
    );

    String blockSql = """
      SELECT start_time, end_time
      FROM public.booking_blocks
      WHERE resource_id = ?
        AND start_time < ?
        AND end_time > ?
      """;

    List<TimeRange> blocks = jdbcTemplate.query(
      blockSql,
      (rs, rowNum) ->
        new TimeRange(
          rs.getTimestamp("start_time").toLocalDateTime(),
          rs.getTimestamp("end_time").toLocalDateTime()
        ),
      resourceId,
      Timestamp.valueOf(rangeEnd),
      Timestamp.valueOf(rangeStart)
    );

    List<WeeklyResourceAvailabilityResponse.DayAvailability> days =
      new ArrayList<>();

    boolean bookable =
      Boolean.TRUE.equals(resource.getActive()) &&
      Boolean.TRUE.equals(resource.getBookable());

    for (int day = 0; day < 7; day++) {
      LocalDate date = startDate.plusDays(day);

      List<WeeklyResourceAvailabilityResponse.SlotAvailability> slots =
        new ArrayList<>();

      for (int hour = 9; hour < 17; hour++) {
        LocalDateTime slotStart = date.atTime(hour, 0);
        LocalDateTime slotEnd = date.atTime(hour + 1, 0);

        boolean bookingConflict = bookings
          .stream()
          .anyMatch(
            range ->
              range.start().isBefore(slotEnd) && range.end().isAfter(slotStart)
          );

        boolean blockConflict = blocks
          .stream()
          .anyMatch(
            range ->
              range.start().isBefore(slotEnd) && range.end().isAfter(slotStart)
          );

        boolean available = bookable && !bookingConflict && !blockConflict;

        slots.add(
          new WeeklyResourceAvailabilityResponse.SlotAvailability(
            slotStart.toLocalTime(),
            slotEnd.toLocalTime(),
            available
          )
        );
      }

      days.add(
        new WeeklyResourceAvailabilityResponse.DayAvailability(date, slots)
      );
    }

    return new WeeklyResourceAvailabilityResponse(resourceId, startDate, days);
  }

  private record TimeRange(LocalDateTime start, LocalDateTime end) {}
}
