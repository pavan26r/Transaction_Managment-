package com.finance.manager.service.impl;

import com.finance.manager.dto.request.GoalRequest;
import com.finance.manager.dto.request.UpdateGoalRequest;
import com.finance.manager.dto.response.ResponseDTOs.*;
import com.finance.manager.entity.CategoryType;
import com.finance.manager.entity.SavingsGoal;
import com.finance.manager.entity.User;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.SavingsGoalRepository;
import com.finance.manager.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SavingsGoalService {

    private final SavingsGoalRepository goalRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryService categoryService;

    public GoalResponse createGoal(GoalRequest request) {
        User user = categoryService.getCurrentUser();

        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be in the future");
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();

        SavingsGoal goal = SavingsGoal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(startDate)
                .user(user)
                .build();

        SavingsGoal saved = goalRepository.save(goal);
        return buildGoalResponse(saved, user);
    }

    public GoalListResponse getAllGoals() {
        User user = categoryService.getCurrentUser();
        List<SavingsGoal> goals = goalRepository.findByUserId(user.getId());
        return new GoalListResponse(goals.stream()
                .map(g -> buildGoalResponse(g, user))
                .collect(Collectors.toList()));
    }

    public GoalResponse getGoal(Long id) {
        User user = categoryService.getCurrentUser();
        SavingsGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied to this goal");
        }

        return buildGoalResponse(goal, user);
    }

    public GoalResponse updateGoal(Long id, UpdateGoalRequest request) {
        User user = categoryService.getCurrentUser();
        SavingsGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied to this goal");
        }

        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }
        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be in the future");
            }
            goal.setTargetDate(request.getTargetDate());
        }

        SavingsGoal saved = goalRepository.save(goal);
        return buildGoalResponse(saved, user);
    }

    public MessageResponse deleteGoal(Long id) {
        User user = categoryService.getCurrentUser();
        SavingsGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));

        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("Access denied to this goal");
        }

        goalRepository.delete(goal);
        return MessageResponse.builder().message("Goal deleted successfully").build();
    }

    private GoalResponse buildGoalResponse(SavingsGoal goal, User user) {
        BigDecimal totalIncome = transactionRepository.sumByUserIdAndTypeAfterDate(
                user.getId(), CategoryType.INCOME, goal.getStartDate());
        BigDecimal totalExpenses = transactionRepository.sumByUserIdAndTypeAfterDate(
                user.getId(), CategoryType.EXPENSE, goal.getStartDate());

        BigDecimal progress = totalIncome.subtract(totalExpenses);
        if (progress.compareTo(BigDecimal.ZERO) < 0) progress = BigDecimal.ZERO;

        BigDecimal target = goal.getTargetAmount();
        BigDecimal remaining = target.subtract(progress);
        if (remaining.compareTo(BigDecimal.ZERO) < 0) remaining = BigDecimal.ZERO;

        double percentage = 0.0;
        if (target.compareTo(BigDecimal.ZERO) > 0) {
            percentage = progress.divide(target, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
            if (percentage > 100.0) percentage = 100.0;
        }

        return GoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .targetDate(goal.getTargetDate())
                .startDate(goal.getStartDate())
                .currentProgress(progress)
                .progressPercentage(Math.round(percentage * 100.0) / 100.0)
                .remainingAmount(remaining)
                .build();
    }
}
