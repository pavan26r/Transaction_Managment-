package com.finance.manager.controller;

import com.finance.manager.dto.response.ResponseDTOs.*;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.service.impl.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Provides monthly and yearly financial reports.
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * Returns a monthly financial report for a given year and month.
     */
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<MonthlyReportResponse> monthly(@PathVariable int year,
                                                          @PathVariable int month) {
        if (month < 1 || month > 12) {
            throw new BadRequestException("Month must be between 1 and 12");
        }
        return ResponseEntity.ok(reportService.getMonthlyReport(year, month));
    }

    /**
     * Returns a yearly financial report for a given year.
     */
    @GetMapping("/yearly/{year}")
    public ResponseEntity<YearlyReportResponse> yearly(@PathVariable int year) {
        return ResponseEntity.ok(reportService.getYearlyReport(year));
    }
}
