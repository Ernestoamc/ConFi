package com.confi.adapter.in.web;

import com.confi.domain.service.PeriodCloseService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/period-close")
public class PeriodCloseController {

    private final PeriodCloseService periodCloseService;

    public PeriodCloseController(PeriodCloseService periodCloseService) {
        this.periodCloseService = periodCloseService;
    }

    @PostMapping
    public PeriodStatusResponse close(@Valid @RequestBody PeriodRequest request) {
        YearMonth period = YearMonth.of(request.year(), request.month());
        periodCloseService.close(period);
        return new PeriodStatusResponse(period.toString(), true);
    }

    @PatchMapping("/reopen")
    public PeriodStatusResponse reopen(@Valid @RequestBody PeriodRequest request) {
        YearMonth period = YearMonth.of(request.year(), request.month());
        periodCloseService.reopen(period);
        return new PeriodStatusResponse(period.toString(), false);
    }

    @GetMapping
    public List<String> listClosed() {
        return periodCloseService.listClosed().stream().map(YearMonth::toString).toList();
    }

    public record PeriodRequest(
            @NotNull @Min(1900) Integer year,
            @NotNull @Min(1) @Max(12) Integer month
    ) {
    }

    public record PeriodStatusResponse(String period, boolean closed) {
    }
}
