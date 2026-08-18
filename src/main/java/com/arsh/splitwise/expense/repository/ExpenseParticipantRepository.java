package com.arsh.splitwise.expense.repository;

import com.arsh.splitwise.expense.entity.Expense;
import com.arsh.splitwise.expense.entity.ExpenseParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseParticipantRepository extends JpaRepository<ExpenseParticipant, Long> {

    List<ExpenseParticipant> findByExpense(Expense expense);

    List<ExpenseParticipant> findByExpenseIn(List<Expense> expenses);
}