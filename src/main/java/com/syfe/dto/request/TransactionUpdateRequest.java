package com.syfe.financemanager.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransactionUpdateRequest {
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String category;
    private String description;
}