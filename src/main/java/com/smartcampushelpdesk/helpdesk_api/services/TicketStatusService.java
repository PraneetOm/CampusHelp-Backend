
package com.smartcampushelpdesk.helpdesk_api.services;

import com.smartcampushelpdesk.helpdesk_api.entities.TicketStatus;
import com.smartcampushelpdesk.helpdesk_api.repositories.TicketStatusRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TicketStatusService {

    private final TicketStatusRepository ticketStatusRepository;

    public TicketStatusService(TicketStatusRepository ticketStatusRepository) {
        this.ticketStatusRepository = ticketStatusRepository;
    }

    public List<TicketStatus> getAllStatuses() {
        return ticketStatusRepository.findAll();
    }
}
