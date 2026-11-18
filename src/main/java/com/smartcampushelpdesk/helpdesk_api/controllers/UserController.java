package com.smartcampushelpdesk.helpdesk_api.controllers;

import com.smartcampushelpdesk.helpdesk_api.entities.Role;
import com.smartcampushelpdesk.helpdesk_api.entities.User;
import com.smartcampushelpdesk.helpdesk_api.services.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(
            Authentication authentication) {

        // Ensure this request has a verified Supabase JWT.
        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)
                || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Valid authentication is required.");
        }

        // JWT subject is the Supabase Auth user UUID.
        UUID userId;

        try {
            userId = UUID.fromString(jwtAuth.getToken().getSubject());
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid user identity in token.");
        }

        return userService.getUserById(userId)
                .map(user -> {
                    String roleName = null;

                    if (user.getRoleId() != null) {
                        roleName = userService
                                .getRoleById(user.getRoleId())
                                .map(Role::getName)
                                .orElse(null);
                    }

                    return ResponseEntity.ok(
                            new UserResponse(
                                    user.getId(),
                                    user.getName(),
                                    user.getEmail(),
                                    user.getPhone(),
                                    user.getStatus(),
                                    user.getCollegeId(),
                                    user.getDepartmentId(),
                                    user.getRoleId(),
                                    roleName
                            )
                    );
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    public record UserResponse(
            UUID id,
            String name,
            String email,
            String phone,
            String status,
            UUID collegeId,
            UUID departmentId,
            UUID roleId,
            String roleName
    ) {}
}