package com.expensemanager.infrastructure.repository;

import com.expensemanager.domain.entity.Expense;
import com.expensemanager.domain.entity.User;
import com.expensemanager.domain.enums.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Expense entity operations.
 */
@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Find all expenses for a specific user.
     *
     * @param user the user
     * @param pageable pagination information
     * @return a page of expenses
     */
    Page<Expense> findByUser(User user, Pageable pageable);

    /**
     * Find expenses by category for a user.
     *
     * @param user the user
     * @param category the expense category
     * @param pageable pagination information
     * @return a page of expenses
     */
    Page<Expense> findByUserAndCategory(User user, ExpenseCategory category, Pageable pageable);

    /**
     * Find expenses within a date range for a user.
     *
     * @param user the user
     * @param startDate the start date
     * @param endDate the end date
     * @return list of expenses in the date range
     */
    List<Expense> findByUserAndExpenseDateBetween(User user, LocalDate startDate, LocalDate endDate);

    /**
     * Get category totals using JPQL aggregation (SUM + GROUP BY).
     * Automatically excludes deleted expenses via @Where annotation.
     *
     * @param userId the user ID
     * @return list of Object[] with [category name, total]
     */
    @Query("SELECT CAST(e.category AS string), SUM(e.amount) FROM Expense e WHERE e.user.id = :userId AND e.isDeleted = false GROUP BY e.category")
    List<Object[]> getCategoryTotals(@Param("userId") Long userId);

    /**
     * Get monthly expense totals using JPQL aggregation with date grouping.
     * Automatically excludes deleted expenses.
     * Groups by YYYY-MM format.
     *
     * @param userId the user ID
     * @return list of Object[] with [YYYY-MM string, total amount]
     */
    @Query("SELECT CONCAT(CAST(YEAR(e.expenseDate) AS string), '-', " +
           "LPAD(CAST(MONTH(e.expenseDate) AS string), 2, '0')), SUM(e.amount) " +
           "FROM Expense e WHERE e.user.id = :userId AND e.isDeleted = false " +
           "GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate) " +
           "ORDER BY YEAR(e.expenseDate) DESC, MONTH(e.expenseDate) DESC")
    List<Object[]> getMonthlySummary(@Param("userId") Long userId);

    /**
     * Get monthly expense totals within a date range.
     * Automatically excludes deleted expenses.
     *
     * @param userId the user ID
     * @param startDate the start date (inclusive)
     * @param endDate the end date (inclusive)
     * @return list of Object[] with [YYYY-MM string, total amount]
     */
    @Query("SELECT CONCAT(CAST(YEAR(e.expenseDate) AS string), '-', " +
           "LPAD(CAST(MONTH(e.expenseDate) AS string), 2, '0')), SUM(e.amount) " +
           "FROM Expense e WHERE e.user.id = :userId AND e.isDeleted = false " +
           "AND e.expenseDate >= :startDate AND e.expenseDate <= :endDate " +
           "GROUP BY YEAR(e.expenseDate), MONTH(e.expenseDate) " +
           "ORDER BY YEAR(e.expenseDate) DESC, MONTH(e.expenseDate) DESC")
    List<Object[]> getMonthlySummaryByDateRange(
        @Param("userId") Long userId,
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate
    );

}

