package com.finance.manager.service.impl;

import com.finance.manager.dto.request.CategoryRequest;
import com.finance.manager.dto.response.ResponseDTOs.*;
import com.finance.manager.entity.CustomCategory;
import com.finance.manager.entity.DefaultCategory;
import com.finance.manager.entity.User;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.exception.ConflictException;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.CustomCategoryRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CustomCategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public CategoryListResponse getAllCategories() {
        User user = getCurrentUser();
        List<CategoryResponse> categories = new ArrayList<>();

        // Add default categories
        for (DefaultCategory dc : DefaultCategory.values()) {
            categories.add(CategoryResponse.builder()
                    .name(dc.getName())
                    .type(dc.getType())
                    .isCustom(false)
                    .build());
        }

        // Add custom categories
        categoryRepository.findByUserId(user.getId()).forEach(cc ->
                categories.add(CategoryResponse.builder()
                        .name(cc.getName())
                        .type(cc.getType())
                        .isCustom(true)
                        .build()));

        return new CategoryListResponse(categories);
    }

    public CategoryResponse createCategory(CategoryRequest request) {
        User user = getCurrentUser();

        // Check if name conflicts with default
        if (DefaultCategory.isDefault(request.getName())) {
            throw new ConflictException("Category name conflicts with a default category");
        }

        // Check uniqueness per user
        if (categoryRepository.existsByNameIgnoreCaseAndUserId(request.getName(), user.getId())) {
            throw new ConflictException("Category with this name already exists");
        }

        CustomCategory category = CustomCategory.builder()
                .name(request.getName())
                .type(request.getType())
                .user(user)
                .build();

        categoryRepository.save(category);

        return CategoryResponse.builder()
                .name(category.getName())
                .type(category.getType())
                .isCustom(true)
                .build();
    }

    public MessageResponse deleteCategory(String name) {
        User user = getCurrentUser();

        // Cannot delete default categories
        if (DefaultCategory.isDefault(name)) {
            throw new ForbiddenException("Cannot delete default categories");
        }

        CustomCategory category = categoryRepository.findByNameIgnoreCaseAndUserId(name, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + name));

        // Check if any transaction uses this category
        if (transactionRepository.existsByUserIdAndCategory(user.getId(), category.getName())) {
            throw new BadRequestException("Cannot delete category that is referenced by transactions");
        }

        categoryRepository.delete(category);
        return MessageResponse.builder().message("Category deleted successfully").build();
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    /**
     * Validates that a category name is accessible to the user (default or custom).
     */
    public boolean isCategoryValid(String name, User user) {
        if (DefaultCategory.isDefault(name)) return true;
        return categoryRepository.existsByNameIgnoreCaseAndUserId(name, user.getId());
    }

    /**
     * Gets the type of a category (default or custom).
     */
    public com.finance.manager.entity.CategoryType getCategoryType(String name, User user) {
        com.finance.manager.entity.CategoryType defaultType = DefaultCategory.getTypeByName(name);
        if (defaultType != null) return defaultType;

        return categoryRepository.findByNameIgnoreCaseAndUserId(name, user.getId())
                .map(CustomCategory::getType)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + name));
    }
}
