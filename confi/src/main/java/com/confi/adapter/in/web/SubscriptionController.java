package com.confi.adapter.in.web;

import com.confi.domain.model.Subscription;
import com.confi.domain.model.Subscription.Frecuencia;
import com.confi.domain.port.in.SubscriptionUseCases;
import com.confi.domain.port.in.SubscriptionUseCases.CreateSubscriptionCommand;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionUseCases subscriptionUseCases;

    public SubscriptionController(SubscriptionUseCases subscriptionUseCases) {
        this.subscriptionUseCases = subscriptionUseCases;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse crear(@jakarta.validation.Valid @RequestBody CreateSubscriptionRequest request) {
        Subscription creada = subscriptionUseCases.crear(new CreateSubscriptionCommand(
                request.nombre(), request.montoEstimado(), request.frecuencia(),
                request.diaCobro(), request.cuentaId(), request.categoriaId()));
        return toResponse(creada);
    }

    @GetMapping
    public List<SubscriptionResponse> listarActivas() {
        return subscriptionUseCases.listarActivas().stream().map(this::toResponse).toList();
    }

    @PatchMapping("/{id}/pausar")
    public void pausar(@PathVariable UUID id) {
        subscriptionUseCases.pausar(id);
    }

    @PatchMapping("/{id}/reactivar")
    public void reactivar(@PathVariable UUID id) {
        subscriptionUseCases.reactivar(id);
    }

    private SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionResponse(s.getId(), s.getNombre(), s.getMontoEstimado(),
                s.getFrecuencia(), s.getDiaCobro(), s.getCuentaId(), s.getCategoriaId(), s.isActiva());
    }

    public record CreateSubscriptionRequest(
            @NotBlank String nombre,
            @NotNull @Positive BigDecimal montoEstimado,
            @NotNull Frecuencia frecuencia,
                @Min(1) @Max(31) int diaCobro,
            @NotNull UUID cuentaId,
            @NotNull UUID categoriaId
    ) {}

    public record SubscriptionResponse(
            UUID id, String nombre, BigDecimal montoEstimado, Frecuencia frecuencia,
            int diaCobro, UUID cuentaId, UUID categoriaId, boolean activa
    ) {}
}