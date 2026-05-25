package com.syfe.financemanager.service;

import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.enums.CategoryType;
import com.syfe.financemanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;

    public Map<String, Object> getMonthlyReport(User user, int year, int month) {
        Map<String, BigDecimal> income = buildCategoryMap(
            transactionRepository.sumByCategoryForMonth(user, month, year, CategoryType.INCOME));
        Map<String, BigDecimal> expenses = buildCategoryMap(
            transactionRepository.sumByCategoryForMonth(user, month, year, CategoryType.EXPENSE));
        BigDecimal netSavings = sumValues(income).subtract(sumValues(expenses));

        Map<String, Object> report = new HashMap<>();
        report.put("month", month);
        report.put("year", year);
        report.put("totalIncome", income);
        report.put("totalExpenses", expenses);
        report.put("netSavings", netSavings);
        return report;
    }

    public Map<String, Object> getYearlyReport(User user, int year) {
        Map<String, BigDecimal> income = buildCategoryMap(
            transactionRepository.sumByCategoryForYear(user, year, CategoryType.INCOME));
        Map<String, BigDecimal> expenses = buildCategoryMap(
            transactionRepository.sumByCategoryForYear(user, year, CategoryType.EXPENSE));
        BigDecimal netSavings = sumValues(income).subtract(sumValues(expenses));

        Map<String, Object> report = new HashMap<>();
        report.put("year", year);
        report.put("totalIncome", income);
        report.put("totalExpenses", expenses);
        report.put("netSavings", netSavings);
        return report;
    }

    private Map<String, BigDecimal> buildCategoryMap(List<Object[]> results) {
        Map<String, BigDecimal> map = new HashMap<>();
        for (Object[] row : results) {
            map.put((String) row[0], (BigDecimal) row[1]);
        }
        return map;
    }

    private BigDecimal sumValues(Map<String, BigDecimal> map) {
        return map.values().stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}