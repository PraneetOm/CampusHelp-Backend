
package com.smartcampushelpdesk.helpdesk_api.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "ticket_statuses", schema = "public")
public class TicketStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "is_terminal")
    private Boolean isTerminal = false;

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsTerminal() {
        return isTerminal;
    }

    public void setIsTerminal(Boolean isTerminal) {
        this.isTerminal = isTerminal;
    }
}
