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
     *
     * @param userId the user ID
     * @return list of Object[] with [category name, total]
     */
    @Query("SELECT CAST(e.category AS string), SUM(e.amount) FROM Expense e WHERE e.user.id = :userId GROUP BY e.category")
    List<Object[]> getCategoryTotals(@Param("userId") Long userId);

}

