
package com.smartcampushelpdesk.helpdesk_api.controllers;

import com.smartcampushelpdesk.helpdesk_api.dto.TicketResponse;
import com.smartcampushelpdesk.helpdesk_api.dto.CreateTicketRequest;
import com.smartcampushelpdesk.helpdesk_api.dto.UpdateTicketStatusRequest;
import com.smartcampushelpdesk.helpdesk_api.dto.TicketAssignmentRequest;
import com.smartcampushelpdesk.helpdesk_api.dto.StaffResponse;
import com.smartcampushelpdesk.helpdesk_api.services.TicketService;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    private UUID getUserId(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Authenticated user not found");
        }
        return UUID.fromString(jwt.getSubject());
    }

    @GetMapping
    public List<TicketResponse> getAllTickets(
            @AuthenticationPrincipal Jwt jwt) {
        return ticketService.getAllTickets(getUserId(jwt));
    }

    @GetMapping("/staff")
    public List<StaffResponse> getAvailableStaff(
            @AuthenticationPrincipal Jwt jwt) {
        return ticketService.getAvailableStaff(getUserId(jwt));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        return ticketService.getTicketById(id, getUserId(jwt))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public List<TicketResponse> searchTickets(
            @RequestParam String keyword,
            @AuthenticationPrincipal Jwt jwt) {
        return ticketService.searchTickets(keyword, getUserId(jwt));
    }

    @GetMapping("/priority/{priority}")
    public List<TicketResponse> getTicketsByPriority(
            @PathVariable String priority,
            @AuthenticationPrincipal Jwt jwt) {
        return ticketService.getTicketsByPriority(
                priority, getUserId(jwt));
    }

    @GetMapping("/status/{statusId}")
    public List<TicketResponse> getTicketsByStatus(
            @PathVariable UUID statusId,
            @AuthenticationPrincipal Jwt jwt) {
        return ticketService.getTicketsByStatus(
                statusId, getUserId(jwt));
    }

    @GetMapping("/page")
    public Page<TicketResponse> getTicketsWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @AuthenticationPrincipal Jwt jwt) {
        return ticketService.getTicketsWithPagination(
                page, size, sortBy, direction, getUserId(jwt));
    }

    @GetMapping("/filter")
    public List<TicketResponse> filterTickets(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) UUID statusId,
            @AuthenticationPrincipal Jwt jwt) {
        return ticketService.filterTickets(
                keyword, priority, statusId, getUserId(jwt));
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        TicketResponse response = ticketService.createTicket(
                request, getUserId(jwt));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable UUID id,
            @Valid @RequestBody TicketAssignmentRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        TicketResponse response = ticketService.assignTicket(
                id, request.getAssignedTo(), getUserId(jwt));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            @AuthenticationPrincipal Jwt jwt) {
        TicketResponse response = ticketService.updateTicketStatus(
                id, request.getStatusId(), getUserId(jwt));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/withdraw")
    public ResponseEntity<TicketResponse> withdrawTicket(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt) {
        TicketResponse response = ticketService.withdrawTicket(
                id, getUserId(jwt));
        return ResponseEntity.ok(response);
    }
}