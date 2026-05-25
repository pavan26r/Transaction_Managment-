package com.finance.manager.service.impl;

import com.finance.manager.dto.request.TransactionRequest;
import com.finance.manager.dto.request.UpdateTransactionRequest;
import com.finance.manager.dto.response.ResponseDTOs.*;
import com.finance.manager.entity.CategoryType;
import com.finance.manager.entity.Transaction;
import com.finance.manager.entity.User;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.TransactionRepository;
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

    public TransactionResponse createTransaction(TransactionRequest request) {
        User user = categoryService.getCurrentUser();

        // Date cannot be future
        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be in the future");
        }

        // Validate category
        if (!categoryService.isCategoryValid(request.getCategory(), user)) {
            throw new BadRequestException("Invalid category: " + request.getCategory());
        }

        CategoryType type = categoryService.getCategoryType(request.getCategory(), user);

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .category(request.getCategory())
                .type(type)
                .description(request.getDescription())
                .user(user)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    public TransactionListResponse getTransactions(LocalDate startDate, LocalDate endDate, String category) {
        User user = categoryService.getCurrentUser();

        // If category filter provided, validate it
        String categoryFilter = null;
        if (category != null && !category.isBlank()) {
            if (!categoryService.isCategoryValid(category, user)) {
                throw new BadRequestException("Invalid category: " + category);
            }
            categoryFilter = category;
        }

        List<Transaction> transactions = transactionRepository.findByUserIdWithFilters(
                user.getId(), startDate, endDate, categoryFilter);

        return new TransactionListResponse(transactions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList()));
    }

    public TransactionResponse updateTransaction(Long id, UpdateTransactionRequest request) {
        User user = categoryService.getCurrentUser();

        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }

        if (request.getCategory() != null) {
            if (!categoryService.isCategoryValid(request.getCategory(), user)) {
                throw new BadRequestException("Invalid category: " + request.getCategory());
            }
            CategoryType type = categoryService.getCategoryType(request.getCategory(), user);
            transaction.setCategory(request.getCategory());
            transaction.setType(type);
        }

        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    public MessageResponse deleteTransaction(Long id) {
        User user = categoryService.getCurrentUser();

        Transaction transaction = transactionRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        transactionRepository.delete(transaction);
        return MessageResponse.builder().message("Transaction deleted successfully").build();
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .date(t.getDate())
                .category(t.getCategory())
                .description(t.getDescription())
                .type(t.getType())
                .build();
    }
}
