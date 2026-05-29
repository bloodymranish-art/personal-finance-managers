package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.request.TransactionRequest;
import com.syfe.financemanager.dto.request.TransactionUpdateRequest;
import com.syfe.financemanager.dto.response.TransactionResponse;
import com.syfe.financemanager.entity.Category;
import com.syfe.financemanager.entity.Transaction;
import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.exception.ResourceNotFoundException;
import com.syfe.financemanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        log.info("Creating transaction for user: {}, amount: {}, category: {}",
            user.getUsername(), request.getAmount(), request.getCategory());

        if (request.getDate().isAfter(LocalDate.now())) {
            log.warn("Transaction creation failed - future date: {}", request.getDate());
            throw new IllegalArgumentException("Transaction date cannot be in the future");
        }

        Category category = categoryService.getCategoryByNameAndUser(
            request.getCategory(), user);

        Transaction transaction = transactionRepository.save(Transaction.builder()
            .amount(request.getAmount())
            .date(request.getDate())
            .description(request.getDescription())
            .category(category)
            .user(user)
            .build());

        log.info("Transaction created successfully with id: {}", transaction.getId());
        return mapToResponse(transaction);
    }

    public List<TransactionResponse> getTransactions(User user, LocalDate startDate,
            LocalDate endDate, Long categoryId) {
        log.info("Fetching transactions for user: {}, startDate: {}, endDate: {}, categoryId: {}",
            user.getUsername(), startDate, endDate, categoryId);

        List<TransactionResponse> result = transactionRepository
            .findByUserOrderByDateDesc(user)
            .stream()
            .filter(t -> startDate == null || !t.getDate().isBefore(startDate))
            .filter(t -> endDate == null || !t.getDate().isAfter(endDate))
            .filter(t -> categoryId == null || t.getCategory().getId().equals(categoryId))
            .map(this::mapToResponse)
            .collect(Collectors.toList());

        log.info("Found {} transactions for user: {}", result.size(), user.getUsername());
        return result;
    }

    public TransactionResponse updateTransaction(Long id,
            TransactionUpdateRequest request, User user) {
        log.info("Updating transaction id: {} for user: {}", id, user.getUsername());

        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> {
                log.warn("Transaction id: {} not found for user: {}", id, user.getUsername());
                return new ResourceNotFoundException("Transaction not found");
            });

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            Category category = categoryService.getCategoryByNameAndUser(
                request.getCategory(), user);
            transaction.setCategory(category);
        }

        Transaction updated = transactionRepository.save(transaction);
        log.info("Transaction id: {} updated successfully", id);
        return mapToResponse(updated);
    }

    public void deleteTransaction(Long id, User user) {
        log.info("Deleting transaction id: {} for user: {}", id, user.getUsername());

        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> {
                log.warn("Transaction id: {} not found for user: {}", id, user.getUsername());
                return new ResourceNotFoundException("Transaction not found");
            });

        transactionRepository.delete(transaction);
        log.info("Transaction id: {} deleted successfully", id);
    }

    private TransactionResponse mapToResponse(Transaction t) {
        return TransactionResponse.builder()
            .id(t.getId())
            .amount(t.getAmount())
            .date(t.getDate())
            .category(t.getCategory().getName())
            .description(t.getDescription())
            .type(t.getCategory().getType())
            .build();
    }
}