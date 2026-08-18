package com.arsh.splitwise.expense.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ExpenseParticipantRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    // Optional when splitType is EQUAL - the service calculates it.
    // Required when splitType is EXACT; that conditional requirement is
    // enforced in ExpenseService rather than here, since it depends on a
    // sibling field. If a value IS provided, it must still be positive.
    @Positive(message = "Amount owed must be positive")
    private BigDecimal amountOwed;
}