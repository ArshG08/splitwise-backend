package com.arsh.splitwise.expense.dto;

import com.arsh.splitwise.expense.enums.ExpenseSplitType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ExpenseResponse {

    private Long id;
    private Long groupId;
    private String description;
    private BigDecimal totalAmount;

    private Long payerId;
    private String payerName;
    private String payerEmail;

    private Long createdById;
    private String createdByName;
    private String createdByEmail;

    private ExpenseSplitType splitType;
    private List<ExpenseParticipantResponse> participants;
    private LocalDateTime createdAt;
}