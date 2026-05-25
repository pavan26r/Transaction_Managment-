package com.finance.manager.repository;

import com.finance.manager.entity.CategoryType;
import com.finance.manager.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserIdOrderByDateDesc(Long userId);

    @Query("SELECT t FROM Transaction t WHERE t.user.id = :userId " +
           "AND (:startDate IS NULL OR t.date >= :startDate) " +
           "AND (:endDate IS NULL OR t.date <= :endDate) " +
           "AND (:category IS NULL OR t.category = :category) " +
           "ORDER BY t.date DESC")
    List<Transaction> findByUserIdWithFilters(@Param("userId") Long userId,
                                              @Param("startDate") LocalDate startDate,
                                              @Param("endDate") LocalDate endDate,
                                              @Param("category") String category);

    Optional<Transaction> findByIdAndUserId(Long id, Long userId);

    boolean existsByUserIdAndCategory(Long userId, String category);

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "AND t.date >= :startDate")
    BigDecimal sumByUserIdAndTypeAfterDate(@Param("userId") Long userId,
                                           @Param("type") CategoryType type,
                                           @Param("startDate") LocalDate startDate);

    @Query("SELECT t.category, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "AND YEAR(t.date) = :year AND MONTH(t.date) = :month " +
           "GROUP BY t.category")
    List<Object[]> sumByCategoryForMonth(@Param("userId") Long userId,
                                         @Param("type") CategoryType type,
                                         @Param("year") int year,
                                         @Param("month") int month);

    @Query("SELECT t.category, COALESCE(SUM(t.amount), 0) FROM Transaction t " +
           "WHERE t.user.id = :userId AND t.type = :type " +
           "AND YEAR(t.date) = :year " +
           "GROUP BY t.category")
    List<Object[]> sumByCategoryForYear(@Param("userId") Long userId,
                                        @Param("type") CategoryType type,
                                        @Param("year") int year);
}
