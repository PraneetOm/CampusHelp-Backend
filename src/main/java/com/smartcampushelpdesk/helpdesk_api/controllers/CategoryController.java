
package com.smartcampushelpdesk.helpdesk_api.controllers;

import com.smartcampushelpdesk.helpdesk_api.entities.Category;
import com.smartcampushelpdesk.helpdesk_api.services.CategoryService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<Category> getAllCategories(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getSubject());

        return categoryService.getAllCategories(userId);
    }
}