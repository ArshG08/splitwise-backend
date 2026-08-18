package com.arsh.splitwise.expense.controller;

import com.arsh.splitwise.expense.dto.CreateExpenseRequest;
import com.arsh.splitwise.expense.dto.ExpenseResponse;
import com.arsh.splitwise.expense.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/groups/{groupId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(
            @PathVariable Long groupId,
            @Valid @RequestBody CreateExpenseRequest request) {

        return expenseService.createExpense(groupId, request);
    }

    @GetMapping
    public List<ExpenseResponse> getGroupExpenses(@PathVariable Long groupId) {
        return expenseService.getGroupExpenses(groupId);
    }

    @GetMapping("/{expenseId}")
    public ExpenseResponse getExpense(
            @PathVariable Long groupId,
            @PathVariable Long expenseId) {

        return expenseService.getExpenseById(groupId, expenseId);
    }
}