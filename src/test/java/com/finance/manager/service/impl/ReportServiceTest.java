package com.finance.manager.service.impl;

import com.finance.manager.dto.response.ResponseDTOs.*;
import com.finance.manager.entity.*;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.service.impl.CategoryService;
import com.finance.manager.service.impl.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryService categoryService;

    @InjectMocks private ReportService reportService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@test.com").build();
        when(categoryService.getCurrentUser()).thenReturn(user);
    }

    @Test
    void getMonthlyReport_Success() {
        // Correct way to build List<Object[]>
        List<Object[]> incomeMock = new ArrayList<>();
        incomeMock.add(new Object[]{"Salary", new BigDecimal("5000")});

        List<Object[]> expenseMock = new ArrayList<>();
        expenseMock.add(new Object[]{"Food", new BigDecimal("500")});

        when(transactionRepository.sumByCategoryForMonth(1L, CategoryType.INCOME, 2024, 1))
                .thenReturn(incomeMock);
        when(transactionRepository.sumByCategoryForMonth(1L, CategoryType.EXPENSE, 2024, 1))
                .thenReturn(expenseMock);

        MonthlyReportResponse res = reportService.getMonthlyReport(2024, 1);
        assertEquals(2024, res.getYear());
        assertEquals(1, res.getMonth());
        assertEquals(new BigDecimal("5000"), res.getTotalIncome().get("Salary"));
        assertEquals(new BigDecimal("4500"), res.getNetSavings());
    }

    @Test
    void getYearlyReport_Success() {
        // Correct way to build List<Object[]>
        List<Object[]> incomeMock = new ArrayList<>();
        incomeMock.add(new Object[]{"Salary", new BigDecimal("60000")});

        List<Object[]> expenseMock = new ArrayList<>();
        expenseMock.add(new Object[]{"Rent", new BigDecimal("12000")});

        when(transactionRepository.sumByCategoryForYear(1L, CategoryType.INCOME, 2024))
                .thenReturn(incomeMock);
        when(transactionRepository.sumByCategoryForYear(1L, CategoryType.EXPENSE, 2024))
                .thenReturn(expenseMock);

        YearlyReportResponse res = reportService.getYearlyReport(2024);
        assertEquals(2024, res.getYear());
        assertEquals(new BigDecimal("48000"), res.getNetSavings());
    }

    @Test
    void getMonthlyReport_NoTransactions_ZeroNetSavings() {
        when(transactionRepository.sumByCategoryForMonth(1L, CategoryType.INCOME, 2024, 6))
                .thenReturn(new ArrayList<>());
        when(transactionRepository.sumByCategoryForMonth(1L, CategoryType.EXPENSE, 2024, 6))
                .thenReturn(new ArrayList<>());

        MonthlyReportResponse res = reportService.getMonthlyReport(2024, 6);
        assertEquals(BigDecimal.ZERO, res.getNetSavings());
    }
}
