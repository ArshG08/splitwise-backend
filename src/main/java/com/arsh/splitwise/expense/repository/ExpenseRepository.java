package com.arsh.splitwise.expense.repository;

import com.arsh.splitwise.expense.entity.Expense;
import com.arsh.splitwise.groups.entity.Group;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByGroupOrderByCreatedAtDesc(Group group);
}