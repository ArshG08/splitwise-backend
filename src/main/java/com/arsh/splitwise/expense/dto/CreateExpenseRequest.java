package com.arsh.splitwise.expense.dto;

import com.arsh.splitwise.expense.enums.ExpenseSplitType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CreateExpenseRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotNull(message = "Payer is required")
    private Long payerId;

    @NotNull(message = "Split type is required")
    private ExpenseSplitType splitType;

    @NotEmpty(message = "At least one participant is required")
    @Valid
    private List<ExpenseParticipantRequest> participants;
}