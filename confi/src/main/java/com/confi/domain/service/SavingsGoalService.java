package com.confi.domain.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class SavingsGoalService {

    private final CopyOnWriteArrayList<SavingsGoal> goals = new CopyOnWriteArrayList<>();

    public SavingsGoal create(String name, BigDecimal targetAmount, Instant targetDate) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name es obligatorio");
        }
        if (targetAmount == null || targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("targetAmount debe ser mayor a cero");
        }
        SavingsGoal goal = new SavingsGoal(
                UUID.randomUUID(),
                name.trim(),
                targetAmount.setScale(2, RoundingMode.HALF_UP),
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                targetDate,
                true
        );
        goals.add(goal);
        return goal;
    }

    public List<SavingsGoal> list() {
        return new ArrayList<>(goals);
    }

    public List<SavingsGoal> snapshot() {
        return new ArrayList<>(goals);
    }

    public int restore(List<SavingsGoal> restoredGoals) {
        goals.clear();
        goals.addAll(restoredGoals);
        return goals.size();
    }

    public SavingsGoal addProgress(UUID id, BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("amount debe ser mayor a cero");
        }
        for (int i = 0; i < goals.size(); i++) {
            SavingsGoal current = goals.get(i);
            if (current.id().equals(id)) {
                BigDecimal updated = current.currentAmount().add(amount).setScale(2, RoundingMode.HALF_UP);
                SavingsGoal next = new SavingsGoal(current.id(), current.name(), current.targetAmount(), updated, current.targetDate(), current.active());
                goals.set(i, next);
                return next;
            }
        }
        throw new NoSuchElementException("Meta de ahorro no encontrada: " + id);
    }

    public SavingsGoal deactivate(UUID id) {
        for (int i = 0; i < goals.size(); i++) {
            SavingsGoal current = goals.get(i);
            if (current.id().equals(id)) {
                SavingsGoal next = new SavingsGoal(current.id(), current.name(), current.targetAmount(), current.currentAmount(), current.targetDate(), false);
                goals.set(i, next);
                return next;
            }
        }
        throw new NoSuchElementException("Meta de ahorro no encontrada: " + id);
    }

    public record SavingsGoal(
            UUID id,
            String name,
            BigDecimal targetAmount,
            BigDecimal currentAmount,
            Instant targetDate,
            boolean active
    ) {
        public BigDecimal progressPercent() {
            if (targetAmount.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return currentAmount
                    .multiply(new BigDecimal("100"))
                    .divide(targetAmount, 2, RoundingMode.HALF_UP);
        }
    }
}
