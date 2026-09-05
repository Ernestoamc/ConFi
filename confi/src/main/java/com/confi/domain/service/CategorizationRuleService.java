package com.confi.domain.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class CategorizationRuleService {

    private final CopyOnWriteArrayList<Rule> rules = new CopyOnWriteArrayList<>();

    public Rule create(String keyword, UUID categoriaId, int priority) {
        if (keyword == null || keyword.isBlank()) {
            throw new IllegalArgumentException("keyword es obligatorio");
        }
        if (categoriaId == null) {
            throw new IllegalArgumentException("categoriaId es obligatorio");
        }
        if (priority < 0) {
            throw new IllegalArgumentException("priority debe ser >= 0");
        }

        Rule rule = new Rule(UUID.randomUUID(), keyword.trim(), categoriaId, priority, true);
        rules.add(rule);
        return rule;
    }

    public List<Rule> list() {
        List<Rule> copy = new ArrayList<>(rules);
        copy.sort(Comparator.comparingInt(Rule::priority).reversed());
        return copy;
    }

    public List<Rule> snapshot() {
        return new ArrayList<>(rules);
    }

    public int restore(List<Rule> restored) {
        rules.clear();
        rules.addAll(restored);
        return rules.size();
    }

    public Rule activate(UUID id) {
        return updateStatus(id, true);
    }

    public Rule deactivate(UUID id) {
        return updateStatus(id, false);
    }

    public UUID resolveCategory(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        String normalized = text.toLowerCase(Locale.ROOT);
        return list().stream()
                .filter(Rule::active)
                .filter(rule -> normalized.contains(rule.keyword().toLowerCase(Locale.ROOT)))
                .map(Rule::categoriaId)
                .findFirst()
                .orElse(null);
    }

    private Rule updateStatus(UUID id, boolean active) {
        for (int i = 0; i < rules.size(); i++) {
            Rule current = rules.get(i);
            if (current.id().equals(id)) {
                Rule updated = new Rule(current.id(), current.keyword(), current.categoriaId(), current.priority(), active);
                rules.set(i, updated);
                return updated;
            }
        }
        throw new NoSuchElementException("Regla de categorizacion no encontrada: " + id);
    }

    public record Rule(UUID id, String keyword, UUID categoriaId, int priority, boolean active) {
    }
}
