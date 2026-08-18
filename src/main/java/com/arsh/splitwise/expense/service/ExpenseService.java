package com.arsh.splitwise.expense.service;

import com.arsh.splitwise.auth.security.CustomUserDetails;
import com.arsh.splitwise.common.exception.InvalidExpenseException;
import com.arsh.splitwise.common.exception.NotActiveGroupMemberException;
import com.arsh.splitwise.common.exception.ResourceNotFoundException;
import com.arsh.splitwise.expense.dto.CreateExpenseRequest;
import com.arsh.splitwise.expense.dto.ExpenseParticipantRequest;
import com.arsh.splitwise.expense.dto.ExpenseParticipantResponse;
import com.arsh.splitwise.expense.dto.ExpenseResponse;
import com.arsh.splitwise.expense.entity.Expense;
import com.arsh.splitwise.expense.entity.ExpenseParticipant;
import com.arsh.splitwise.expense.enums.ExpenseSplitType;
import com.arsh.splitwise.expense.repository.ExpenseParticipantRepository;
import com.arsh.splitwise.expense.repository.ExpenseRepository;
import com.arsh.splitwise.groups.entity.Group;
import com.arsh.splitwise.groups.entity.GroupMember;
import com.arsh.splitwise.groups.enums.GroupMemberStatus;
import com.arsh.splitwise.groups.repository.GroupMemberRepository;
import com.arsh.splitwise.groups.repository.GroupRepository;
import com.arsh.splitwise.user.entity.User;
import com.arsh.splitwise.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseParticipantRepository expenseParticipantRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;

    @Transactional
    public ExpenseResponse createExpense(Long groupId, CreateExpenseRequest request) {

        User currentUser = getCurrentUser();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        requireActiveMembership(group, currentUser,
                "User is not an active member of this group");

        User payer = userRepository.findById(request.getPayerId())
                .orElseThrow(() -> new ResourceNotFoundException("Payer not found"));

        requireActiveMembership(group, payer,
                "Payer is not an active member of this group");

        List<ExpenseParticipantRequest> participantRequests = request.getParticipants();

        checkNoDuplicateParticipants(participantRequests);

        List<User> participantUsers = resolveActiveParticipants(group, participantRequests);

        List<BigDecimal> splitAmounts = computeSplitAmounts(request, participantRequests);

        Expense expense = Expense.builder()
                .group(group)
                .description(request.getDescription())
                .totalAmount(request.getTotalAmount())
                .payer(payer)
                .createdBy(currentUser)
                .splitType(request.getSplitType())
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        List<ExpenseParticipant> participants = new ArrayList<>();
        for (int i = 0; i < participantUsers.size(); i++) {
            participants.add(
                    ExpenseParticipant.builder()
                            .expense(savedExpense)
                            .user(participantUsers.get(i))
                            .amountOwed(splitAmounts.get(i))
                            .build()
            );
        }

        List<ExpenseParticipant> savedParticipants =
                expenseParticipantRepository.saveAll(participants);

        return buildExpenseResponse(savedExpense, savedParticipants);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getGroupExpenses(Long groupId) {

        User currentUser = getCurrentUser();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        requireActiveMembership(group, currentUser,
                "User is not an active member of this group");

        List<Expense> expenses = expenseRepository.findByGroupOrderByCreatedAtDesc(group);

        List<ExpenseParticipant> allParticipants =
                expenseParticipantRepository.findByExpenseIn(expenses);

        Map<Long, List<ExpenseParticipant>> participantsByExpenseId = allParticipants.stream()
                .collect(Collectors.groupingBy(p -> p.getExpense().getId()));

        return expenses.stream()
                .map(expense -> buildExpenseResponse(
                        expense,
                        participantsByExpenseId.getOrDefault(expense.getId(), List.of())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse getExpenseById(Long groupId, Long expenseId) {

        User currentUser = getCurrentUser();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Group not found"));

        requireActiveMembership(group, currentUser,
                "User is not an active member of this group");

        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found"));

        if (!expense.getGroup().getId().equals(groupId)) {
            throw new ResourceNotFoundException("Expense does not belong to this group");
        }

        List<ExpenseParticipant> participants =
                expenseParticipantRepository.findByExpense(expense);

        return buildExpenseResponse(expense, participants);
    }

    // ----- helpers -----

    private User getCurrentUser() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        return userDetails.getUser();
    }

    private GroupMember requireActiveMembership(Group group, User user, String errorMessage) {

        GroupMember membership = groupMemberRepository.findByGroupAndUser(group, user)
                .orElseThrow(() -> new NotActiveGroupMemberException(errorMessage));

        if (membership.getStatus() != GroupMemberStatus.ACTIVE) {
            throw new NotActiveGroupMemberException(errorMessage);
        }

        return membership;
    }

    private void checkNoDuplicateParticipants(List<ExpenseParticipantRequest> participantRequests) {

        Set<Long> seenUserIds = new HashSet<>();

        for (ExpenseParticipantRequest participantRequest : participantRequests) {
            if (!seenUserIds.add(participantRequest.getUserId())) {
                throw new InvalidExpenseException("Duplicate participant");
            }
        }
    }

    private List<User> resolveActiveParticipants(
            Group group,
            List<ExpenseParticipantRequest> participantRequests) {

        List<User> participants = new ArrayList<>();

        for (ExpenseParticipantRequest participantRequest : participantRequests) {

            User participant = userRepository.findById(participantRequest.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Participant not found"));

            requireActiveMembership(group, participant,
                    "Participant is not an active member of this group");

            participants.add(participant);
        }

        return participants;
    }

    private List<BigDecimal> computeSplitAmounts(
            CreateExpenseRequest request,
            List<ExpenseParticipantRequest> participantRequests) {

        if (request.getSplitType() == ExpenseSplitType.EQUAL) {
            return computeEqualSplit(request.getTotalAmount(), participantRequests.size());
        }

        return computeExactSplit(request.getTotalAmount(), participantRequests);
    }

    // Rounds each participant's share DOWN to the nearest cent, then hands the
    // leftover cents out one at a time to the first few participants, so the
    // shares always add back up to exactly the original total.
    // Example: 100 split 3 ways -> base share 33.33, 0.01 left over
    // -> first participant gets 33.34, the other two get 33.33.
    private List<BigDecimal> computeEqualSplit(BigDecimal totalAmount, int participantCount) {

        BigDecimal count = BigDecimal.valueOf(participantCount);

        BigDecimal baseShare = totalAmount.divide(count, 2, RoundingMode.DOWN);
        BigDecimal distributed = baseShare.multiply(count);
        BigDecimal remainder = totalAmount.subtract(distributed);

        int extraCentRecipients = remainder.movePointRight(2).intValue();
        BigDecimal oneCent = new BigDecimal("0.01");

        List<BigDecimal> shares = new ArrayList<>();
        for (int i = 0; i < participantCount; i++) {
            shares.add(i < extraCentRecipients ? baseShare.add(oneCent) : baseShare);
        }

        return shares;
    }

    private List<BigDecimal> computeExactSplit(
            BigDecimal totalAmount,
            List<ExpenseParticipantRequest> participantRequests) {

        List<BigDecimal> amounts = new ArrayList<>();
        BigDecimal sum = BigDecimal.ZERO;

        for (ExpenseParticipantRequest participantRequest : participantRequests) {

            BigDecimal amount = participantRequest.getAmountOwed();

            if (amount == null) {
                throw new InvalidExpenseException(
                        "Each participant's amount is required for an exact split");
            }

            amounts.add(amount);
            sum = sum.add(amount);
        }

        if (sum.compareTo(totalAmount) != 0) {
            throw new InvalidExpenseException("Exact split amounts must equal total amount");
        }

        return amounts;
    }

    private ExpenseResponse buildExpenseResponse(
            Expense expense,
            List<ExpenseParticipant> participants) {

        List<ExpenseParticipantResponse> participantResponses = participants.stream()
                .map(participant -> ExpenseParticipantResponse.builder()
                        .userId(participant.getUser().getId())
                        .name(participant.getUser().getName())
                        .email(participant.getUser().getEmail())
                        .amountOwed(participant.getAmountOwed())
                        .build())
                .toList();

        return ExpenseResponse.builder()
                .id(expense.getId())
                .groupId(expense.getGroup().getId())
                .description(expense.getDescription())
                .totalAmount(expense.getTotalAmount())
                .payerId(expense.getPayer().getId())
                .payerName(expense.getPayer().getName())
                .payerEmail(expense.getPayer().getEmail())
                .createdById(expense.getCreatedBy().getId())
                .createdByName(expense.getCreatedBy().getName())
                .createdByEmail(expense.getCreatedBy().getEmail())
                .splitType(expense.getSplitType())
                .participants(participantResponses)
                .createdAt(expense.getCreatedAt())
                .build();
    }
}