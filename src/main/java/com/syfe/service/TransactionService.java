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
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    public TransactionResponse createTransaction(TransactionRequest request, User user) {
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Transaction date cannot be in the future");
        }
        Category category = categoryService.getCategoryByNameAndUser(request.getCategory(), user);
        Transaction transaction = transactionRepository.save(Transaction.builder()
            .amount(request.getAmount())
            .date(request.getDate())
            .description(request.getDescription())
            .category(category)
            .user(user)
            .build());
        return mapToResponse(transaction);
    }

    public List<TransactionResponse> getTransactions(User user, LocalDate startDate,
            LocalDate endDate, Long categoryId) {
        return transactionRepository.findByUserOrderByDateDesc(user)
            .stream()
            .filter(t -> startDate == null || !t.getDate().isBefore(startDate))
            .filter(t -> endDate == null || !t.getDate().isAfter(endDate))
            .filter(t -> categoryId == null || t.getCategory().getId().equals(categoryId))
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public TransactionResponse updateTransaction(Long id, TransactionUpdateRequest request, User user) {
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        if (request.getAmount() != null) transaction.setAmount(request.getAmount());
        if (request.getDescription() != null) transaction.setDescription(request.getDescription());
        if (request.getCategory() != null) {
            Category category = categoryService.getCategoryByNameAndUser(request.getCategory(), user);
            transaction.setCategory(category);
        }
        return mapToResponse(transactionRepository.save(transaction));
    }

    public void deleteTransaction(Long id, User user) {
        Transaction transaction = transactionRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));
        transactionRepository.delete(transaction);
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