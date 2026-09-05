package com.confi.adapter.in.web;

import com.confi.domain.service.CategorizationRuleService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/categorization-rules")
public class CategorizationRuleController {

    private final CategorizationRuleService service;

    public CategorizationRuleController(CategorizationRuleService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RuleResponse create(@Valid @RequestBody CreateRuleRequest request) {
        return toResponse(service.create(request.keyword(), request.categoriaId(), request.priority()));
    }

    @GetMapping
    public List<RuleResponse> list() {
        return service.list().stream().map(CategorizationRuleController::toResponse).toList();
    }

    @PatchMapping("/{id}/activate")
    public RuleResponse activate(@PathVariable UUID id) {
        return toResponse(service.activate(id));
    }

    @PatchMapping("/{id}/deactivate")
    public RuleResponse deactivate(@PathVariable UUID id) {
        return toResponse(service.deactivate(id));
    }

    @GetMapping("/resolve")
    public ResolveResponse resolve(@RequestParam String text) {
        return new ResolveResponse(service.resolveCategory(text));
    }

    private static RuleResponse toResponse(CategorizationRuleService.Rule rule) {
        return new RuleResponse(rule.id(), rule.keyword(), rule.categoriaId(), rule.priority(), rule.active());
    }

    public record CreateRuleRequest(
            @NotBlank String keyword,
            @NotNull UUID categoriaId,
            @Min(0) int priority
    ) {
    }

    public record RuleResponse(
            UUID id,
            String keyword,
            UUID categoriaId,
            int priority,
            boolean active
    ) {
    }

    public record ResolveResponse(UUID categoriaId) {
    }
}
