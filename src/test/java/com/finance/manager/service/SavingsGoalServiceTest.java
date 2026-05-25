package com.finance.manager.service;

import com.finance.manager.dto.request.GoalRequest;
import com.finance.manager.dto.request.UpdateGoalRequest;
import com.finance.manager.dto.response.ResponseDTOs.*;
import com.finance.manager.entity.*;
import com.finance.manager.exception.BadRequestException;
import com.finance.manager.exception.ForbiddenException;
import com.finance.manager.exception.ResourceNotFoundException;
import com.finance.manager.repository.SavingsGoalRepository;
import com.finance.manager.repository.TransactionRepository;
import com.finance.manager.service.impl.CategoryService;
import com.finance.manager.service.impl.SavingsGoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalServiceTest {

    @Mock private SavingsGoalRepository goalRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryService categoryService;

    @InjectMocks private SavingsGoalService goalService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("test@test.com").build();
        when(categoryService.getCurrentUser()).thenReturn(user);
    }

    private void mockProgress() {
        when(transactionRepository.sumByUserIdAndTypeAfterDate(eq(1L), eq(CategoryType.INCOME), any()))
                .thenReturn(new BigDecimal("2000"));
        when(transactionRepository.sumByUserIdAndTypeAfterDate(eq(1L), eq(CategoryType.EXPENSE), any()))
                .thenReturn(new BigDecimal("500"));
    }

    @Test
    void createGoal_Success() {
        GoalRequest req = new GoalRequest();
        req.setGoalName("Emergency Fund");
        req.setTargetAmount(new BigDecimal("5000"));
        req.setTargetDate(LocalDate.now().plusMonths(6));

        SavingsGoal saved = SavingsGoal.builder().id(1L).goalName("Emergency Fund")
                .targetAmount(new BigDecimal("5000")).targetDate(req.getTargetDate())
                .startDate(LocalDate.now()).user(user).build();
        when(goalRepository.save(any())).thenReturn(saved);
        mockProgress();

        GoalResponse res = goalService.createGoal(req);
        assertEquals(1L, res.getId());
        assertEquals(new BigDecimal("1500"), res.getCurrentProgress());
    }

    @Test
    void createGoal_PastTargetDate_ThrowsBadRequest() {
        GoalRequest req = new GoalRequest();
        req.setGoalName("Test");
        req.setTargetAmount(new BigDecimal("1000"));
        req.setTargetDate(LocalDate.now().minusDays(1));

        assertThrows(BadRequestException.class, () -> goalService.createGoal(req));
    }

    @Test
    void getGoal_OtherUserGoal_ThrowsForbidden() {
        User otherUser = User.builder().id(2L).build();
        SavingsGoal goal = SavingsGoal.builder().id(1L).user(otherUser)
                .targetAmount(BigDecimal.TEN).startDate(LocalDate.now()).build();
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));

        assertThrows(ForbiddenException.class, () -> goalService.getGoal(1L));
    }

    @Test
    void getGoal_NotFound_Throws() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> goalService.getGoal(99L));
    }

    @Test
    void getAllGoals_Success() {
        SavingsGoal g = SavingsGoal.builder().id(1L).goalName("Vacation")
                .targetAmount(new BigDecimal("3000")).targetDate(LocalDate.now().plusYears(1))
                .startDate(LocalDate.now()).user(user).build();
        when(goalRepository.findByUserId(1L)).thenReturn(List.of(g));
        mockProgress();

        GoalListResponse res = goalService.getAllGoals();
        assertEquals(1, res.getGoals().size());
    }

    @Test
    void deleteGoal_Success() {
        SavingsGoal g = SavingsGoal.builder().id(1L).user(user).build();
        when(goalRepository.findById(1L)).thenReturn(Optional.of(g));

        MessageResponse res = goalService.deleteGoal(1L);
        assertEquals("Goal deleted successfully", res.getMessage());
        verify(goalRepository).delete(g);
    }

    @Test
    void updateGoal_Success() {
        SavingsGoal g = SavingsGoal.builder().id(1L).goalName("Fund")
                .targetAmount(new BigDecimal("5000")).targetDate(LocalDate.now().plusYears(1))
                .startDate(LocalDate.now()).user(user).build();
        when(goalRepository.findById(1L)).thenReturn(Optional.of(g));
        when(goalRepository.save(any())).thenReturn(g);
        mockProgress();

        UpdateGoalRequest req = new UpdateGoalRequest();
        req.setTargetAmount(new BigDecimal("6000"));

        GoalResponse res = goalService.updateGoal(1L, req);
        assertEquals(new BigDecimal("6000"), res.getTargetAmount());
    }
}
