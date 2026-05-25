package com.syfe.financemanager.dto.request;

import com.syfe.financemanager.enums.CategoryType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class CategoryRequest {
    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Category type is required")
    private CategoryType type;
}