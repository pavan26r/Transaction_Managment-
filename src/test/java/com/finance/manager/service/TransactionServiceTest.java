package com.finance.manager.service;

import com.finance.manager.dto.request.TransactionRequest;
import com.finance.manager.dto.request.UpdateTransactionRequest;
import com.finance.manager.dto.response.ResponseDTOs.*;
import com.finance.manager.entity.*;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.service.impl.CategoryService;
import com.finance.manager.service.impl.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryService categoryService;

    @InjectMocks private TransactionService transactionService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@test.com").build();
        when(categoryService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void createTransaction_Success() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("5000"));
        req.setDate(LocalDate.now());
        req.setCategory("Salary");

        when(categoryService.isCategoryValid("Salary", user)).thenReturn(true);
        when(categoryService.getCategoryType("Salary", user)).thenReturn(CategoryType.INCOME);

        Transaction saved = Transaction.builder()
                .id(1L).amount(req.getAmount()).date(req.getDate())
                .category("Salary").type(CategoryType.INCOME).user(user).build();
        when(transactionRepository.save(any())).thenReturn(saved);

        TransactionResponse res = transactionService.createTransaction(req);
        assertEquals(1L, res.getId());
        assertEquals(CategoryType.INCOME, res.getType());
    }

    @Test
    void createTransaction_FutureDate_ThrowsBadRequest() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("100"));
        req.setDate(LocalDate.now().plusDays(1));
        req.setCategory("Salary");

        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(req));
    }

    @Test
    void createTransaction_InvalidCategory_ThrowsBadRequest() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(new BigDecimal("100"));
        req.setDate(LocalDate.now());
        req.setCategory("InvalidCat");

        when(categoryService.isCategoryValid("InvalidCat", user)).thenReturn(false);
        assertThrows(BadRequestException.class, () -> transactionService.createTransaction(req));
    }

    @Test
    void getTransactions_ReturnsAll() {
        Transaction t = Transaction.builder().id(1L).amount(BigDecimal.TEN)
                .date(LocalDate.now()).category("Food").type(CategoryType.EXPENSE).user(user).build();
        when(transactionRepository.findByUserIdWithFilters(eq(1L), any(), any(), any()))
                .thenReturn(List.of(t));

        TransactionListResponse res = transactionService.getTransactions(null, null, null);
        assertEquals(1, res.getTransactions().size());
    }

    @Test
    void updateTransaction_Success() {
        Transaction existing = Transaction.builder().id(1L).amount(BigDecimal.TEN)
                .date(LocalDate.now()).category("Food").type(CategoryType.EXPENSE).user(user).build();
        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(existing));
        when(transactionRepository.save(any())).thenReturn(existing);

        UpdateTransactionRequest req = new UpdateTransactionRequest();
        req.setAmount(new BigDecimal("200"));

        TransactionResponse res = transactionService.updateTransaction(1L, req);
        assertEquals(new BigDecimal("200"), res.getAmount());
    }

    @Test
    void updateTransaction_NotFound_Throws() {
        when(transactionRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.updateTransaction(99L, new UpdateTransactionRequest()));
    }

    @Test
    void deleteTransaction_Success() {
        Transaction t = Transaction.builder().id(1L).user(user).build();
        when(transactionRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(t));

        MessageResponse res = transactionService.deleteTransaction(1L);
        assertEquals("Transaction deleted successfully", res.getMessage());
        verify(transactionRepository).delete(t);
    }

    @Test
    void deleteTransaction_NotFound_Throws() {
        when(transactionRepository.findByIdAndUserId(99L, 1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transactionService.deleteTransaction(99L));
    }
}
