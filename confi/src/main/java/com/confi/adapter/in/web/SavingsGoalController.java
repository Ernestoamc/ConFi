package com.confi.adapter.in.web;

import com.confi.domain.service.SavingsGoalService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/savings-goals")
public class SavingsGoalController {

    private final SavingsGoalService service;

    public SavingsGoalController(SavingsGoalService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GoalResponse create(@Valid @RequestBody CreateGoalRequest request) {
        return toResponse(service.create(request.name(), request.targetAmount(), request.targetDate()));
    }

    @GetMapping
    public List<GoalResponse> list() {
        return service.list().stream().map(SavingsGoalController::toResponse).toList();
    }

    @PatchMapping("/{id}/progress")
    public GoalResponse addProgress(@PathVariable UUID id, @Valid @RequestBody AddProgressRequest request) {
        return toResponse(service.addProgress(id, request.amount()));
    }

    @PatchMapping("/{id}/deactivate")
    public GoalResponse deactivate(@PathVariable UUID id) {
        return toResponse(service.deactivate(id));
    }

    private static GoalResponse toResponse(SavingsGoalService.SavingsGoal goal) {
        return new GoalResponse(
                goal.id(),
                goal.name(),
                goal.targetAmount(),
                goal.currentAmount(),
                goal.progressPercent(),
                goal.targetDate(),
                goal.active()
        );
    }

    public record CreateGoalRequest(
            @NotBlank String name,
            @NotNull @Positive BigDecimal targetAmount,
            Instant targetDate
    ) {
    }

    public record AddProgressRequest(@NotNull @Positive BigDecimal amount) {
    }

    public record GoalResponse(
            UUID id,
            String name,
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            BigDecimal progressPercent,
            Instant targetDate,
            boolean active
    ) {
    }
}
