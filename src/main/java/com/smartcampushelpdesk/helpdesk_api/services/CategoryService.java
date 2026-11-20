
package com.smartcampushelpdesk.helpdesk_api.services;

import com.smartcampushelpdesk.helpdesk_api.entities.Category;
import com.smartcampushelpdesk.helpdesk_api.entities.User;
import com.smartcampushelpdesk.helpdesk_api.repositories.CategoryRepository;
import com.smartcampushelpdesk.helpdesk_api.repositories.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public CategoryService(
            CategoryRepository categoryRepository,
            UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    public List<Category> getAllCategories(UUID authenticatedUserId) {

        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User profile not found"
                ));

        if (user.getStatus() == null ||
                !user.getStatus().equalsIgnoreCase("ACTIVE")) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your account is inactive"
            );
        }

        if (user.getCollegeId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "College information is missing"
            );
        }

        return categoryRepository.findByCollegeId(user.getCollegeId());
    }
}