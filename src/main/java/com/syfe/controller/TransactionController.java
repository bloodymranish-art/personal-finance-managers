package com.syfe.financemanager.controller;

import com.syfe.financemanager.dto.request.TransactionRequest;
import com.syfe.financemanager.dto.request.TransactionUpdateRequest;
import com.syfe.financemanager.dto.response.TransactionResponse;
import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.service.AuthService;
import com.syfe.financemanager.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;
    private final AuthService authService;

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("POST /api/transactions - user: {}", userDetails.getUsername());
        User user = authService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(transactionService.createTransaction(request, user));
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getTransactions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        log.info("GET /api/transactions - user: {}, page: {}, size: {}",
            userDetails.getUsername(), page, size);

        User user = authService.getUserByUsername(userDetails.getUsername());

        List<TransactionResponse> allTransactions =
            transactionService.getTransactions(user, startDate, endDate, categoryId);

        // Pagination logic
        int totalItems = allTransactions.size();
        int totalPages = (int) Math.ceil((double) totalItems / size);
        int start = Math.min(page * size, totalItems);
        int end = Math.min(start + size, totalItems);
        List<TransactionResponse> pagedTransactions = allTransactions.subList(start, end);

        Map<String, Object> response = new HashMap<>();
        response.put("transactions", pagedTransactions);
        response.put("totalItems", totalItems);
        response.put("currentPage", page);
        response.put("totalPages", totalPages);
        response.put("pageSize", size);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("PUT /api/transactions/{} - user: {}", id, userDetails.getUsername());
        User user = authService.getUserByUsername(userDetails.getUsername());
        return ResponseEntity.ok(
            transactionService.updateTransaction(id, request, user));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTransaction(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        log.info("DELETE /api/transactions/{} - user: {}", id, userDetails.getUsername());
        User user = authService.getUserByUsername(userDetails.getUsername());
        transactionService.deleteTransaction(id, user);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Transaction deleted successfully");
        return ResponseEntity.ok(response);
    }
}