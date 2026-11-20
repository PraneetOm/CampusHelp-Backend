package com.smartcampushelpdesk.helpdesk_api.services;

import com.smartcampushelpdesk.helpdesk_api.dto.CreateTicketRequest;
import com.smartcampushelpdesk.helpdesk_api.dto.StaffResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.TicketResponse;
import com.smartcampushelpdesk.helpdesk_api.entities.Role;
import com.smartcampushelpdesk.helpdesk_api.entities.Ticket;
import com.smartcampushelpdesk.helpdesk_api.entities.TicketStatus;
import com.smartcampushelpdesk.helpdesk_api.entities.User;
import com.smartcampushelpdesk.helpdesk_api.repositories.CategoryRepository;
import com.smartcampushelpdesk.helpdesk_api.repositories.RoleRepository;
import com.smartcampushelpdesk.helpdesk_api.repositories.TicketRepository;
import com.smartcampushelpdesk.helpdesk_api.repositories.TicketStatusRepository;
import com.smartcampushelpdesk.helpdesk_api.repositories.UserRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TicketService {

  private final TicketRepository ticketRepository;
  private final TicketStatusRepository ticketStatusRepository;
  private final UserRepository userRepository;
  private final RoleRepository roleRepository;
  private final CategoryRepository categoryRepository;

  public TicketService(
    TicketRepository ticketRepository,
    TicketStatusRepository ticketStatusRepository,
    UserRepository userRepository,
    RoleRepository roleRepository,
    CategoryRepository categoryRepository
  ) {
    this.ticketRepository = ticketRepository;
    this.ticketStatusRepository = ticketStatusRepository;
    this.userRepository = userRepository;
    this.roleRepository = roleRepository;
    this.categoryRepository = categoryRepository;
  }

  private User getActiveUser(UUID userId) {
    User user = userRepository
      .findById(userId)
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "User profile not found"
        )
      );

    if (
      user.getStatus() == null || !user.getStatus().equalsIgnoreCase("ACTIVE")
    ) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Your account is inactive"
      );
    }

    if (user.getRoleId() == null) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "User role not found"
      );
    }

    if (user.getCollegeId() == null) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "User college information is missing"
      );
    }

    return user;
  }

  private String getRoleName(User user) {
    Role role = roleRepository
      .findById(user.getRoleId())
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.FORBIDDEN, "User role not found")
      );

    return role.getName() == null
      ? ""
      : role.getName().trim().toUpperCase(Locale.ROOT);
  }

  private void requireAdmin(User user) {
    if (!"ADMIN".equals(getRoleName(user))) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Only administrators can perform this action"
      );
    }
  }

  private TicketResponse convertToResponse(Ticket ticket) {
    TicketResponse response = new TicketResponse();

    response.setId(ticket.getId());
    response.setTicketNumber(ticket.getTicketNumber());
    response.setTitle(ticket.getTitle());
    response.setDescription(ticket.getDescription());
    response.setLocation(ticket.getLocation());
    response.setCategoryId(ticket.getCategoryId());
    response.setPriority(ticket.getPriority());
    response.setStatusId(ticket.getStatusId());
    response.setCreatedAt(ticket.getCreatedAt());
    response.setDueDate(ticket.getDueDate());
    response.setCreatedBy(ticket.getCreatedBy());

    String creatorName = userRepository
      .findById(ticket.getCreatedBy())
      .map(User::getName)
      .orElse("Unknown User");

    response.setCreatedByName(creatorName);
    response.setAssignedTo(ticket.getAssignedTo());

    return response;
  }

  /*
   * Ticket visibility:
   * ADMIN   -> all tickets of own college
   * STUDENT -> only tickets created by the student
   * STAFF   -> tickets created by staff OR assigned to staff
   */

  private List<Ticket> getAccessibleTickets(User user) {
    String role = getRoleName(user);

    return switch (role) {
      case "ADMIN" -> ticketRepository.findByCollegeIdOrderByCreatedAtDesc(
        user.getCollegeId()
      );
      case "STUDENT" -> ticketRepository.findByCollegeIdAndCreatedByOrderByCreatedAtDesc(
        user.getCollegeId(),
        user.getId()
      );
      case "STAFF" -> ticketRepository.findStaffAccessibleTickets(
        user.getCollegeId(),
        user.getId()
      );
      default -> throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "You are not allowed to view tickets"
      );
    };
  }

  private Ticket getTicketForCollege(UUID ticketId, User user) {
    Ticket ticket = ticketRepository
      .findById(ticketId)
      .orElseThrow(() ->
        new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found")
      );

    if (
      ticket.getCollegeId() == null ||
      !user.getCollegeId().equals(ticket.getCollegeId())
    ) {
      throw new ResponseStatusException(
        HttpStatus.NOT_FOUND,
        "Ticket not found"
      );
    }

    return ticket;
  }

  // CREATE TICKET
  public TicketResponse createTicket(
    CreateTicketRequest request,
    UUID authenticatedUserId
  ) {
    User user = getActiveUser(authenticatedUserId);

    String roleName = getRoleName(user);

    if (!roleName.equals("STUDENT") && !roleName.equals("STAFF")) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Only students and staff can raise tickets"
      );
    }

    if (request.getTitle() == null || request.getTitle().isBlank()) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Ticket title is required"
      );
    }

    if (
      request.getDescription() == null || request.getDescription().isBlank()
    ) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Ticket description is required"
      );
    }

    // Validate that the selected category belongs to this user's college.
    if (request.getCategoryId() != null) {
      categoryRepository
        .findByIdAndCollegeId(request.getCategoryId(), user.getCollegeId())
        .orElseThrow(() ->
          new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Invalid category for your college"
          )
        );
    }

    TicketStatus openStatus = ticketStatusRepository
      .findByNameIgnoreCase("OPEN")
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "OPEN ticket status is not configured"
        )
      );

    Ticket ticket = new Ticket();

    ticket.setTicketNumber(
      "TKT-" + UUID.randomUUID().toString().toUpperCase(Locale.ROOT)
    );

    ticket.setTitle(request.getTitle().trim());
    ticket.setDescription(request.getDescription().trim());
    ticket.setCategoryId(request.getCategoryId());
    ticket.setLocation(request.getLocation());

    ticket.setPriority(
      request.getPriority() == null || request.getPriority().isBlank()
        ? "MEDIUM"
        : request.getPriority().trim().toUpperCase(Locale.ROOT)
    );

    ticket.setDueDate(request.getDueDate());

    ticket.setCreatedBy(user.getId());
    ticket.setCollegeId(user.getCollegeId());
    ticket.setStatusId(openStatus.getId());

    Ticket savedTicket = ticketRepository.save(ticket);

    return convertToResponse(savedTicket);
  }

  // GET TICKETS ACCORDING TO ROLE
  public List<TicketResponse> getAllTickets(UUID authenticatedUserId) {
    User user = getActiveUser(authenticatedUserId);

    return getAccessibleTickets(user)
      .stream()
      .map(this::convertToResponse)
      .collect(Collectors.toList());
  }

  // GET ONE TICKET
  public Optional<TicketResponse> getTicketById(
    UUID id,
    UUID authenticatedUserId
  ) {
    User user = getActiveUser(authenticatedUserId);

    return getAccessibleTickets(user)
      .stream()
      .filter(ticket -> ticket.getId().equals(id))
      .findFirst()
      .map(this::convertToResponse);
  }

  // SEARCH TICKETS
  public List<TicketResponse> searchTickets(
    String keyword,
    UUID authenticatedUserId
  ) {
    User user = getActiveUser(authenticatedUserId);

    String search =
      keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

    return getAccessibleTickets(user)
      .stream()
      .filter(
        ticket ->
          (ticket.getTitle() != null &&
            ticket.getTitle().toLowerCase(Locale.ROOT).contains(search)) ||
          (ticket.getDescription() != null &&
            ticket.getDescription().toLowerCase(Locale.ROOT).contains(search))
      )
      .map(this::convertToResponse)
      .collect(Collectors.toList());
  }

  // FILTER BY PRIORITY
  public List<TicketResponse> getTicketsByPriority(
    String priority,
    UUID authenticatedUserId
  ) {
    User user = getActiveUser(authenticatedUserId);

    return getAccessibleTickets(user)
      .stream()
      .filter(
        ticket ->
          ticket.getPriority() != null &&
          ticket.getPriority().equalsIgnoreCase(priority)
      )
      .map(this::convertToResponse)
      .collect(Collectors.toList());
  }

  // FILTER BY STATUS
  public List<TicketResponse> getTicketsByStatus(
    UUID statusId,
    UUID authenticatedUserId
  ) {
    User user = getActiveUser(authenticatedUserId);

    return getAccessibleTickets(user)
      .stream()
      .filter(ticket -> statusId.equals(ticket.getStatusId()))
      .map(this::convertToResponse)
      .collect(Collectors.toList());
  }

  // PAGINATION
  public Page<TicketResponse> getTicketsWithPagination(
    int page,
    int size,
    String sortBy,
    String direction,
    UUID authenticatedUserId
  ) {
    if (page < 0) {
      throw new IllegalArgumentException("Page number cannot be negative");
    }

    if (size < 1 || size > 100) {
      throw new IllegalArgumentException("Page size must be between 1 and 100");
    }

    List<String> allowedFields = List.of(
      "createdAt",
      "updatedAt",
      "title",
      "priority",
      "ticketNumber",
      "dueDate"
    );

    if (!allowedFields.contains(sortBy)) {
      throw new IllegalArgumentException("Invalid sorting field");
    }

    if (
      !"asc".equalsIgnoreCase(direction) && !"desc".equalsIgnoreCase(direction)
    ) {
      throw new IllegalArgumentException("Invalid sort direction");
    }

    User user = getActiveUser(authenticatedUserId);

    List<Ticket> scopedTickets = new ArrayList<>(getAccessibleTickets(user));

    Comparator<Ticket> comparator = switch (sortBy) {
      case "title" -> Comparator.comparing(
        Ticket::getTitle,
        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
      );
      case "priority" -> Comparator.comparing(
        Ticket::getPriority,
        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
      );
      case "ticketNumber" -> Comparator.comparing(
        Ticket::getTicketNumber,
        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)
      );
      case "dueDate" -> Comparator.comparing(
        Ticket::getDueDate,
        Comparator.nullsLast(Comparator.naturalOrder())
      );
      case "updatedAt" -> Comparator.comparing(
        Ticket::getUpdatedAt,
        Comparator.nullsLast(Comparator.naturalOrder())
      );
      default -> Comparator.comparing(
        Ticket::getCreatedAt,
        Comparator.nullsLast(Comparator.naturalOrder())
      );
    };

    if ("desc".equalsIgnoreCase(direction)) {
      comparator = comparator.reversed();
    }

    scopedTickets.sort(comparator);

    int start = Math.min(page * size, scopedTickets.size());
    int end = Math.min(start + size, scopedTickets.size());

    List<TicketResponse> content = scopedTickets
      .subList(start, end)
      .stream()
      .map(this::convertToResponse)
      .collect(Collectors.toList());

    Pageable pageable = PageRequest.of(
      page,
      size,
      Sort.by(
        direction.equalsIgnoreCase("desc")
          ? Sort.Direction.DESC
          : Sort.Direction.ASC,
        sortBy
      )
    );

    return new PageImpl<>(content, pageable, scopedTickets.size());
  }

  // COMBINED FILTER
  public List<TicketResponse> filterTickets(
    String keyword,
    String priority,
    UUID statusId,
    UUID authenticatedUserId
  ) {
    User user = getActiveUser(authenticatedUserId);

    String search =
      keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);

    return getAccessibleTickets(user)
      .stream()
      .filter(
        ticket ->
          search.isEmpty() ||
          (ticket.getTitle() != null &&
            ticket.getTitle().toLowerCase(Locale.ROOT).contains(search)) ||
          (ticket.getDescription() != null &&
            ticket.getDescription().toLowerCase(Locale.ROOT).contains(search))
      )
      .filter(
        ticket ->
          priority == null ||
          priority.isBlank() ||
          (ticket.getPriority() != null &&
            ticket.getPriority().equalsIgnoreCase(priority))
      )
      .filter(
        ticket -> statusId == null || statusId.equals(ticket.getStatusId())
      )
      .map(this::convertToResponse)
      .collect(Collectors.toList());
  }

  // GET ACTIVE STAFF OF ADMIN'S COLLEGE
  public List<StaffResponse> getAvailableStaff(UUID authenticatedUserId) {
    User admin = getActiveUser(authenticatedUserId);

    requireAdmin(admin);

    return userRepository
      .findByCollegeIdAndStatusIgnoreCase(admin.getCollegeId(), "ACTIVE")
      .stream()
      .filter(user -> "STAFF".equals(getRoleName(user)))
      .map(user ->
        new StaffResponse(user.getId(), user.getName(), user.getEmail())
      )
      .collect(Collectors.toList());
  }

  // ASSIGN OR REASSIGN TICKET
  public TicketResponse assignTicket(
    UUID ticketId,
    UUID staffId,
    UUID authenticatedUserId
  ) {
    User admin = getActiveUser(authenticatedUserId);

    requireAdmin(admin);

    Ticket ticket = getTicketForCollege(ticketId, admin);

    TicketStatus currentStatus = ticketStatusRepository
      .findById(ticket.getStatusId())
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Current ticket status not found"
        )
      );

    if ("WITHDRAWN".equalsIgnoreCase(currentStatus.getName())) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Withdrawn tickets cannot be assigned"
      );
    }

    User staff = userRepository
      .findById(staffId)
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Staff member not found"
        )
      );

    if (
      staff.getStatus() == null || !staff.getStatus().equalsIgnoreCase("ACTIVE")
    ) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Only active staff members can be assigned"
      );
    }

    if (
      staff.getCollegeId() == null ||
      !admin.getCollegeId().equals(staff.getCollegeId())
    ) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Staff member belongs to another college"
      );
    }

    if (!"STAFF".equals(getRoleName(staff))) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Selected user is not a staff member"
      );
    }

    ticket.setAssignedTo(staff.getId());

    return convertToResponse(ticketRepository.save(ticket));
  }

  // UPDATE TICKET STATUS
  public TicketResponse updateTicketStatus(
    UUID ticketId,
    UUID statusId,
    UUID authenticatedUserId
  ) {
    User user = getActiveUser(authenticatedUserId);

    String role = getRoleName(user);

    Ticket ticket = getTicketForCollege(ticketId, user);

    TicketStatus currentStatus = ticketStatusRepository
      .findById(ticket.getStatusId())
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Current ticket status not found"
        )
      );

    if ("WITHDRAWN".equalsIgnoreCase(currentStatus.getName())) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Withdrawn tickets cannot be updated"
      );
    }

    if ("ADMIN".equals(role)) {
      // Admin can update tickets of own college.
    } else if ("STAFF".equals(role)) {
      if (
        ticket.getAssignedTo() == null ||
        !user.getId().equals(ticket.getAssignedTo())
      ) {
        throw new ResponseStatusException(
          HttpStatus.FORBIDDEN,
          "Staff can update only tickets assigned to them"
        );
      }
    } else {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Students cannot update ticket status"
      );
    }

    TicketStatus targetStatus = ticketStatusRepository
      .findById(statusId)
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Ticket status not found"
        )
      );

    if ("WITHDRAWN".equalsIgnoreCase(targetStatus.getName())) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Use the dedicated withdrawal action to withdraw tickets"
      );
    }

    ticket.setStatusId(targetStatus.getId());

    if ("RESOLVED".equalsIgnoreCase(targetStatus.getName())) {
      ticket.setResolvedAt(LocalDateTime.now());
    }

    if ("CLOSED".equalsIgnoreCase(targetStatus.getName())) {
      ticket.setClosedAt(LocalDateTime.now());
    }

    return convertToResponse(ticketRepository.save(ticket));
  }

  // WITHDRAW OWN TICKET - STUDENT / STAFF ONLY
  public TicketResponse withdrawTicket(
    UUID ticketId,
    UUID authenticatedUserId
  ) {
    User user = getActiveUser(authenticatedUserId);

    String role = getRoleName(user);

    if (!"STUDENT".equals(role) && !"STAFF".equals(role)) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "Only students and staff can withdraw tickets"
      );
    }

    Ticket ticket = getTicketForCollege(ticketId, user);

    // User can withdraw only a ticket created by themselves.
    if (
      ticket.getCreatedBy() == null ||
      !user.getId().equals(ticket.getCreatedBy())
    ) {
      throw new ResponseStatusException(
        HttpStatus.FORBIDDEN,
        "You can withdraw only tickets raised by you"
      );
    }

    TicketStatus currentStatus = ticketStatusRepository
      .findById(ticket.getStatusId())
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.NOT_FOUND,
          "Current ticket status not found"
        )
      );

    String currentStatusName =
      currentStatus.getName() == null
        ? ""
        : currentStatus.getName().trim().toUpperCase(Locale.ROOT);

    if (
      !List.of("OPEN", "IN_PROGRESS", "REOPENED").contains(currentStatusName)
    ) {
      throw new ResponseStatusException(
        HttpStatus.BAD_REQUEST,
        "Only Open, In Progress or Reopened tickets can be withdrawn"
      );
    }

    TicketStatus withdrawnStatus = ticketStatusRepository
      .findByNameIgnoreCase("WITHDRAWN")
      .orElseThrow(() ->
        new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR,
          "WITHDRAWN ticket status is not configured"
        )
      );

    ticket.setStatusId(withdrawnStatus.getId());

    Ticket savedTicket = ticketRepository.save(ticket);

    return convertToResponse(savedTicket);
  }
}
