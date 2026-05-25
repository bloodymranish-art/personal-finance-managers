package com.syfe.financemanager.repository;

import com.syfe.financemanager.entity.Category;
import com.syfe.financemanager.entity.Transaction;
import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.enums.CategoryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByUserOrderByDateDesc(User user);
    Optional<Transaction> findByIdAndUser(Long id, User user);
    boolean existsByCategory(Category category);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user = :user AND t.category.type = :type " +
           "AND t.date >= :startDate")
    BigDecimal sumByUserAndTypeAndDateAfter(
        @Param("user") User user,
        @Param("type") CategoryType type,
        @Param("startDate") LocalDate startDate);

    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t " +
           "WHERE t.user = :user AND MONTH(t.date) = :month " +
           "AND YEAR(t.date) = :year AND t.category.type = :type " +
           "GROUP BY t.category.name")
    List<Object[]> sumByCategoryForMonth(
        @Param("user") User user,
        @Param("month") int month,
        @Param("year") int year,
        @Param("type") CategoryType type);

    @Query("SELECT t.category.name, SUM(t.amount) FROM Transaction t " +
           "WHERE t.user = :user AND YEAR(t.date) = :year " +
           "AND t.category.type = :type GROUP BY t.category.name")
    List<Object[]> sumByCategoryForYear(
        @Param("user") User user,
        @Param("year") int year,
        @Param("type") CategoryType type);
}