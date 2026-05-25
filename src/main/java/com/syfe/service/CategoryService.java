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
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;

    public List<CategoryResponse> getAllCategories(User user) {
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
        if (categoryRepository.existsByNameAndUser(request.getName(), user)) {
            throw new ConflictException("Category name already exists");
        }
        Category category = categoryRepository.save(Category.builder()
            .name(request.getName())
            .type(request.getType())
            .custom(true)
            .user(user)
            .build());
        return CategoryResponse.builder()
            .name(category.getName())
            .type(category.getType())
            .isCustom(category.isCustom())
            .build();
    }

    public void deleteCategory(String name, User user) {
        Category category = categoryRepository.findByNameAndUser(name, user)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (!category.isCustom()) {
            throw new ForbiddenException("Cannot delete default categories");
        }
        if (transactionRepository.existsByCategory(category)) {
            throw new IllegalArgumentException("Category is in use by transactions");
        }
        categoryRepository.delete(category);
    }

    public Category getCategoryByNameAndUser(String name, User user) {
        return categoryRepository.findByNameAndUser(name, user)
            .orElseGet(() -> categoryRepository.findByNameAndUserIsNull(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + name)));
    }
}