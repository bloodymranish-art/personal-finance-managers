package com.syfe.financemanager.config;

import com.syfe.financemanager.entity.Category;
import com.syfe.financemanager.enums.CategoryType;
import com.syfe.financemanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        createDefaultCategory("Salary", CategoryType.INCOME);
        createDefaultCategory("Food", CategoryType.EXPENSE);
        createDefaultCategory("Rent", CategoryType.EXPENSE);
        createDefaultCategory("Transportation", CategoryType.EXPENSE);
        createDefaultCategory("Entertainment", CategoryType.EXPENSE);
        createDefaultCategory("Healthcare", CategoryType.EXPENSE);
        createDefaultCategory("Utilities", CategoryType.EXPENSE);
    }

    private void createDefaultCategory(String name, CategoryType type) {
        if (categoryRepository.findByNameAndUserIsNull(name).isEmpty()) {
            categoryRepository.save(Category.builder()
                .name(name)
                .type(type)
                .custom(false)
                .user(null)
                .build());
        }
    }
}