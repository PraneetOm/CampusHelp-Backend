package com.smartcampushelpdesk.helpdesk_api.controllers;

import com.smartcampushelpdesk.helpdesk_api.entities.TicketStatus;
import com.smartcampushelpdesk.helpdesk_api.services.TicketStatusService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ticket-statuses")
public class TicketStatusController {

    private final TicketStatusService ticketStatusService;

    public TicketStatusController(TicketStatusService ticketStatusService) {
        this.ticketStatusService = ticketStatusService;
    }

    @GetMapping
    public List<TicketStatus> getAllStatuses() {
        return ticketStatusService.getAllStatuses();
    }
}