package com.smartcampushelpdesk.helpdesk_api.services;

import com.smartcampushelpdesk.helpdesk_api.entities.User;
import com.smartcampushelpdesk.helpdesk_api.entities.Role;
import com.smartcampushelpdesk.helpdesk_api.repositories.UserRepository;
import com.smartcampushelpdesk.helpdesk_api.repositories.RoleRepository;

import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    public Optional<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<Role> getRoleById(UUID roleId) {
        return roleRepository.findById(roleId);
    }

    public Optional<Role> getUserRole(UUID userId) {
        return userRepository.findById(userId)
                .map(User::getRoleId)
                .flatMap(roleRepository::findById);
    }
}