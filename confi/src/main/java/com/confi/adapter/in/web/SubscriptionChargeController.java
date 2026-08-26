package com.confi.adapter.in.web;

import com.confi.domain.model.SubscriptionCharge;
import com.confi.domain.model.SubscriptionCharge.Estado;
import com.confi.domain.port.in.GenerateMonthlyChargesUseCase;
import com.confi.domain.port.in.SubscriptionChargeUseCases;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscription-charges")
@Validated
public class SubscriptionChargeController {

    private final GenerateMonthlyChargesUseCase generateMonthlyChargesUseCase;
    private final SubscriptionChargeUseCases subscriptionChargeUseCases;

    public SubscriptionChargeController(GenerateMonthlyChargesUseCase generateMonthlyChargesUseCase,
                                         SubscriptionChargeUseCases subscriptionChargeUseCases) {
        this.generateMonthlyChargesUseCase = generateMonthlyChargesUseCase;
        this.subscriptionChargeUseCases = subscriptionChargeUseCases;
    }

    /** Genera los cargos PENDIENTES del mes para cada suscripción activa (idempotente). */
    @PostMapping("/generar")
    public List<SubscriptionChargeResponse> generar(
            @RequestParam @Min(1) @Max(12) int mes,
            @RequestParam @Min(1900) int anio) {
        return generateMonthlyChargesUseCase.execute(mes, anio).stream().map(this::toResponse).toList();
    }

    @GetMapping
    public List<SubscriptionChargeResponse> listarPorMes(
            @RequestParam @Min(1) @Max(12) int mes,
            @RequestParam @Min(1900) int anio) {
        return subscriptionChargeUseCases.listarPorMes(mes, anio).stream().map(this::toResponse).toList();
    }

    @PostMapping("/{id}/confirmar")
    public SubscriptionChargeResponse confirmar(@PathVariable UUID id) {
        return toResponse(subscriptionChargeUseCases.confirmar(id));
    }

    @PostMapping("/{id}/omitir")
    public SubscriptionChargeResponse omitir(@PathVariable UUID id) {
        return toResponse(subscriptionChargeUseCases.omitir(id));
    }

    private SubscriptionChargeResponse toResponse(SubscriptionCharge c) {
        return new SubscriptionChargeResponse(c.getId(), c.getSubscripcionId(), c.getFechaEsperada(),
                c.getMontoEsperado(), c.getEstado(), c.getTransactionId());
    }

    public record SubscriptionChargeResponse(
            UUID id, UUID subscripcionId, LocalDate fechaEsperada,
            BigDecimal montoEsperado, Estado estado, UUID transactionId
    ) {}
}