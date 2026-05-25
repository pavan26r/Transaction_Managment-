package com.finance.manager.controller;

import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.ResponseDTOs.*;
import com.finance.manager.service.impl.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Manages transaction categories (default and custom).
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * Returns all categories (default + user's custom).
     */
    @GetMapping
    public ResponseEntity<CategoryListResponse> getAll() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * Creates a custom category for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.createCategory(request));
    }

    /**
     * Deletes a custom category by name.
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> delete(@PathVariable String name) {
        return ResponseEntity.ok(categoryService.deleteCategory(name));
    }
}
