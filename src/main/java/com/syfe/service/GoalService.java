package com.syfe.financemanager.service;

import com.syfe.financemanager.dto.request.GoalRequest;
import com.syfe.financemanager.dto.request.GoalUpdateRequest;
import com.syfe.financemanager.dto.response.GoalResponse;
import com.syfe.financemanager.entity.SavingsGoal;
import com.syfe.financemanager.entity.User;
import com.syfe.financemanager.enums.CategoryType;
import com.syfe.financemanager.exception.ForbiddenException;
import com.syfe.financemanager.exception.ResourceNotFoundException;
import com.syfe.financemanager.repository.SavingsGoalRepository;
import com.syfe.financemanager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {

    private final SavingsGoalRepository goalRepository;
    private final TransactionRepository transactionRepository;

    public GoalResponse createGoal(GoalRequest request, User user) {
        log.info("Creating goal '{}' for user: {}", request.getGoalName(), user.getUsername());

        LocalDate startDate = request.getStartDate() != null
            ? request.getStartDate() : LocalDate.now();

        SavingsGoal goal = goalRepository.save(SavingsGoal.builder()
            .goalName(request.getGoalName())
            .targetAmount(request.getTargetAmount())
            .targetDate(request.getTargetDate())
            .startDate(startDate)
            .user(user)
            .build());

        log.info("Goal '{}' created with id: {}", goal.getGoalName(), goal.getId());
        return mapToResponse(goal, user);
    }

    public List<GoalResponse> getAllGoals(User user) {
        log.info("Fetching all goals for user: {}", user.getUsername());
        return goalRepository.findByUser(user)
            .stream()
            .map(g -> mapToResponse(g, user))
            .collect(Collectors.toList());
    }

    public GoalResponse getGoal(Long id, User user) {
        log.info("Fetching goal id: {} for user: {}", id, user.getUsername());
        SavingsGoal goal = goalRepository.findByIdAndUser(id, user)
            .orElseThrow(() -> {
                log.warn("Goal id: {} not found for user: {}", id, user.getUsername());
                return new ResourceNotFoundException("Goal not found");
            });
        return mapToResponse(goal, user);
    }

    public GoalResponse updateGoal(Long id, GoalUpdateRequest request, User user) {
        log.info("Updating goal id: {} for user: {}", id, user.getUsername());

        SavingsGoal goal = goalRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Goal id: {} not found", id);
                return new ResourceNotFoundException("Goal not found");
            });

        if (!goal.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized access - user: {} tried to update goal: {}",
                user.getUsername(), id);
            throw new ForbiddenException("Access denied");
        }

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getTargetDate() != null) {
            goal.setTargetDate(request.getTargetDate());
        }

        SavingsGoal updated = goalRepository.save(goal);
        log.info("Goal id: {} updated successfully", id);
        return mapToResponse(updated, user);
    }

    public void deleteGoal(Long id, User user) {
        log.info("Deleting goal id: {} for user: {}", id, user.getUsername());

        SavingsGoal goal = goalRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Goal id: {} not found", id);
                return new ResourceNotFoundException("Goal not found");
            });

        if (!goal.getUser().getId().equals(user.getId())) {
            log.warn("Unauthorized access - user: {} tried to delete goal: {}",
                user.getUsername(), id);
            throw new ForbiddenException("Access denied");
        }

        goalRepository.delete(goal);
        log.info("Goal id: {} deleted successfully", id);
    }

    private GoalResponse mapToResponse(SavingsGoal goal, User user) {
        BigDecimal income = transactionRepository.sumByUserAndTypeAndDateAfter(
            user, CategoryType.INCOME, goal.getStartDate());
        BigDecimal expense = transactionRepository.sumByUserAndTypeAndDateAfter(
            user, CategoryType.EXPENSE, goal.getStartDate());

        BigDecimal progress = income.subtract(expense);
        BigDecimal percentage = goal.getTargetAmount().compareTo(BigDecimal.ZERO) == 0
            ? BigDecimal.ZERO
            : progress.divide(goal.getTargetAmount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal remaining = goal.getTargetAmount().subtract(progress);

        return GoalResponse.builder()
            .id(goal.getId())
            .goalName(goal.getGoalName())
            .targetAmount(goal.getTargetAmount())
            .targetDate(goal.getTargetDate())
            .startDate(goal.getStartDate())
            .currentProgress(progress)
            .progressPercentage(percentage)
            .remainingAmount(remaining)
            .build();
    }
}