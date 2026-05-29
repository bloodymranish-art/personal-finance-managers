package com.syfe.financemanager;

import com.syfe.financemanager.dto.request.TransactionRequest;
import com.syfe.financemanager.entity.Category;
import com.syfe.financemanager.entity.Transaction;
import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.enums.CategoryType;
import com.syfe.financemanager.exception.ResourceNotFoundException;
import com.syfe.financemanager.repository.TransactionRepository;
import com.syfe.financemanager.service.CategoryService;
import com.syfe.financemanager.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock TransactionRepository transactionRepository;
    @Mock CategoryService categoryService;
    @InjectMocks TransactionService transactionService;

    private User mockUser;
    private Category mockCategory;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
            .id(1L)
            .username("test@gmail.com")
            .password("password")
            .build();
        mockCategory = Category.builder()
            .id(1L)
            .name("Salary")
            .type(CategoryType.INCOME)
            .build();
    }

    @Test
    void shouldCreateTransactionSuccessfully() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(BigDecimal.valueOf(5000));
        req.setDate(LocalDate.now());
        req.setCategory("Salary");
        req.setDescription("Test");

        Transaction saved = Transaction.builder()
            .id(1L)
            .amount(req.getAmount())
            .date(req.getDate())
            .category(mockCategory)
            .user(mockUser)
            .description("Test")
            .build();

        when(categoryService.getCategoryByNameAndUser("Salary", mockUser))
            .thenReturn(mockCategory);
        when(transactionRepository.save(any()))
            .thenReturn(saved);

        var result = transactionService.createTransaction(req, mockUser);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(5000), result.getAmount());
        assertEquals(CategoryType.INCOME, result.getType());
    }

    @Test
    void shouldThrowWhenDateIsFuture() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(BigDecimal.valueOf(100));
        req.setDate(LocalDate.now().plusDays(1));
        req.setCategory("Salary");

        assertThrows(IllegalArgumentException.class,
            () -> transactionService.createTransaction(req, mockUser));
    }

    @Test
    void shouldThrowWhenTransactionNotFound() {
        when(transactionRepository.findByIdAndUser(99L, mockUser))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> transactionService.deleteTransaction(99L, mockUser));
    }

    @Test
    void shouldDeleteTransactionSuccessfully() {
        Transaction tx = Transaction.builder()
            .id(1L)
            .amount(BigDecimal.valueOf(100))
            .category(mockCategory)
            .user(mockUser)
            .build();

        when(transactionRepository.findByIdAndUser(1L, mockUser))
            .thenReturn(Optional.of(tx));

        assertDoesNotThrow(
            () -> transactionService.deleteTransaction(1L, mockUser));
        verify(transactionRepository, times(1)).delete(tx);
    }
}