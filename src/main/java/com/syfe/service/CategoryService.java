package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.request.CategoryRequest;
import com.syfe.financemanager.dto.response.CategoryResponse;
import com.syfe.financemanager.entity.Category;
import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.exception.ConflictException;
import com.syfe.financemanager.exception.ForbiddenException;
import com.syfe.financemanager.exception.ResourceNotFoundException;
import com.syfe.financemanager.repository.CategoryRepository;
import com.syfe.financemanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public List<CategoryResponse> getAllCategories(User user) {
        log.info("Fetching all categories for user: {}", user.getUsername());
        return categoryRepository.findByUserIsNullOrUser(user)
            .stream()
            .map(c -> CategoryResponse.builder()
                .name(c.getName())
                .type(c.getType())
                .isCustom(c.isCustom())
                .build())
            .collect(Collectors.toList());
    }

    public CategoryResponse createCategory(CategoryRequest request, User user) {
        log.info("Creating category '{}' for user: {}", request.getName(), user.getUsername());
        if (categoryRepository.existsByNameAndUser(request.getName(), user)) {
            log.warn("Category creation failed - duplicate name '{}' for user: {}",
                request.getName(), user.getUsername());
            throw new ConflictException("Category name already exists");
        }
        Category category = categoryRepository.save(Category.builder()
            .name(request.getName())
            .type(request.getType())
            .custom(true)
            .user(user)
            .build());
        log.info("Category '{}' created successfully", category.getName());
        return CategoryResponse.builder()
            .name(category.getName())
            .type(category.getType())
            .isCustom(category.isCustom())
            .build();
    }

    public void deleteCategory(String name, User user) {
        log.info("Deleting category '{}' for user: {}", name, user.getUsername());

        // Check default category first
        if (categoryRepository.findByNameAndUserIsNull(name).isPresent()) {
            log.warn("Attempt to delete default category '{}'", name);
            throw new ForbiddenException("Cannot delete default categories");
        }

        Category category = categoryRepository.findByNameAndUser(name, user)
            .orElseThrow(() -> {
                log.warn("Category '{}' not found for user: {}", name, user.getUsername());
                return new ResourceNotFoundException("Category not found");
            });

        if (transactionRepository.existsByCategory(category)) {
            log.warn("Cannot delete category '{}' - in use by transactions", name);
            throw new IllegalArgumentException("Category is in use by transactions");
        }

        categoryRepository.delete(category);
        log.info("Category '{}' deleted successfully", name);
    }

    public Category getCategoryByNameAndUser(String name, User user) {
        log.debug("Looking up category '{}' for user: {}", name, user.getUsername());
        return categoryRepository.findByNameAndUser(name, user)
            .orElseGet(() -> categoryRepository.findByNameAndUserIsNull(name)
                .orElseThrow(() -> {
                    log.warn("Category '{}' not found", name);
                    return new ResourceNotFoundException("Category not found: " + name);
                }));
    }
}