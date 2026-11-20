package com.smartcampushelpdesk.helpdesk_api.repositories;

import com.smartcampushelpdesk.helpdesk_api.entities.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketStatusRepository
        extends JpaRepository<TicketStatus, UUID> {

    Optional<TicketStatus> findByNameIgnoreCase(String name);
}