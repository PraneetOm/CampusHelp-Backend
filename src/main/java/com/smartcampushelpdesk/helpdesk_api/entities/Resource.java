
package com.smartcampushelpdesk.helpdesk_api.entities;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "resources", schema = "public")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "college_id", nullable = false)
    private UUID collegeId;

    @Column(name = "resource_type_id")
    private UUID resourceTypeId;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(nullable = false)
    private String name;

    private String code;

    private String description;

    private Integer capacity;

    @Column(name = "is_bookable")
    private Boolean bookable;

    @Column(name = "is_active")
    private Boolean active;

    // Constructors

    public Resource() {
    }

    // Getters and Setters

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getCollegeId() {
        return collegeId;
    }

    public void setCollegeId(UUID collegeId) {
        this.collegeId = collegeId;
    }

    public UUID getResourceTypeId() {
        return resourceTypeId;
    }

    public void setResourceTypeId(UUID resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }

    public UUID getRoomId() {
        return roomId;
    }

    public void setRoomId(UUID roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public Boolean getBookable() {
        return bookable;
    }

    public void setBookable(Boolean bookable) {
        this.bookable = bookable;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
