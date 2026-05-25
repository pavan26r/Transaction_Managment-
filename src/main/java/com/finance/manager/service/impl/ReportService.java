package com.finance.manager.service.impl;

import com.finance.manager.dto.response.ResponseDTOs.*;
import com.finance.manager.entity.CategoryType;
import com.finance.manager.entity.User;
import com.finance.manager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    public MonthlyReportResponse getMonthlyReport(int year, int month) {
        User user = categoryService.getCurrentUser();

        Map<String, BigDecimal> incomeMap = buildCategoryMap(
                transactionRepository.sumByCategoryForMonth(user.getId(), CategoryType.INCOME, year, month));
        Map<String, BigDecimal> expenseMap = buildCategoryMap(
                transactionRepository.sumByCategoryForMonth(user.getId(), CategoryType.EXPENSE, year, month));

        BigDecimal totalIncome = incomeMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenseMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return MonthlyReportResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(incomeMap)
                .totalExpenses(expenseMap)
                .netSavings(netSavings)
                .build();
    }

    public YearlyReportResponse getYearlyReport(int year) {
        User user = categoryService.getCurrentUser();

        Map<String, BigDecimal> incomeMap = buildCategoryMap(
                transactionRepository.sumByCategoryForYear(user.getId(), CategoryType.INCOME, year));
        Map<String, BigDecimal> expenseMap = buildCategoryMap(
                transactionRepository.sumByCategoryForYear(user.getId(), CategoryType.EXPENSE, year));

        BigDecimal totalIncome = incomeMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenseMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return YearlyReportResponse.builder()
                .year(year)
                .totalIncome(incomeMap)
                .totalExpenses(expenseMap)
                .netSavings(netSavings)
                .build();
    }

    private Map<String, BigDecimal> buildCategoryMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], (BigDecimal) row[1]);
        }
        return map;
    }
}
