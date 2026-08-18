package com.arsh.splitwise.expense.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ExpenseParticipantResponse {

    private Long userId;
    private String name;
    private String email;
    private BigDecimal amountOwed;
}