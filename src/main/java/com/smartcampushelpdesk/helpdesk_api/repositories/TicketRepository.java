
package com.smartcampushelpdesk.helpdesk_api.repositories;

import com.smartcampushelpdesk.helpdesk_api.entities.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {

    // ADMIN: all tickets belonging to their college
    List<Ticket> findByCollegeIdOrderByCreatedAtDesc(UUID collegeId);

    // STUDENT: only tickets created by them
    List<Ticket> findByCollegeIdAndCreatedByOrderByCreatedAtDesc(
            UUID collegeId,
            UUID createdBy
    );

    // STAFF: tickets created by them OR assigned to them
    @Query("""
        SELECT t FROM Ticket t
        WHERE t.collegeId = :collegeId
        AND (t.createdBy = :userId OR t.assignedTo = :userId)
        ORDER BY t.createdAt DESC
    """)
    List<Ticket> findStaffAccessibleTickets(
            @Param("collegeId") UUID collegeId,
            @Param("userId") UUID userId
    );
}