package com.smartcampushelpdesk.helpdesk_api.repositories;

import com.smartcampushelpdesk.helpdesk_api.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByEmail(String email);

}
