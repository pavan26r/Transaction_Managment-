package com.finance.manager.controller;

import com.finance.manager.dto.request.GoalRequest;
import com.finance.manager.dto.request.UpdateGoalRequest;
import com.finance.manager.dto.response.ResponseDTOs.*;
import com.finance.manager.service.impl.SavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Manages savings goals for the authenticated user.
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final SavingsGoalService goalService;

    /**
     * Creates a new savings goal.
     */
    @PostMapping
    public ResponseEntity<GoalResponse> create(@Valid @RequestBody GoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.createGoal(request));
    }

    /**
     * Returns all savings goals for the authenticated user.
     */
    @GetMapping
    public ResponseEntity<GoalListResponse> getAll() {
        return ResponseEntity.ok(goalService.getAllGoals());
    }

    /**
     * Returns a specific savings goal by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.getGoal(id));
    }

    /**
     * Updates a savings goal's target amount or date.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody UpdateGoalRequest request) {
        return ResponseEntity.ok(goalService.updateGoal(id, request));
    }

    /**
     * Deletes a savings goal.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.deleteGoal(id));
    }
}
