package com.syfe.financemanager.dto.response;

import com.syfe.financemanager.enums.CategoryType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private String name;
    private CategoryType type;
    private boolean isCustom;
}