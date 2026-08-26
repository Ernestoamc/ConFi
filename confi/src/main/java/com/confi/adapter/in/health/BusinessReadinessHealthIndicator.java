package com.confi.adapter.in.health;

import com.confi.domain.port.in.AccountUseCases;
import com.confi.domain.port.in.SubscriptionChargeUseCases;
import com.confi.domain.port.in.SubscriptionUseCases;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Component("businessReadiness")
public class BusinessReadinessHealthIndicator implements HealthIndicator {

    private final AccountUseCases accountUseCases;
    private final SubscriptionUseCases subscriptionUseCases;
    private final SubscriptionChargeUseCases subscriptionChargeUseCases;

    public BusinessReadinessHealthIndicator(
            AccountUseCases accountUseCases,
            SubscriptionUseCases subscriptionUseCases,
            SubscriptionChargeUseCases subscriptionChargeUseCases) {
        this.accountUseCases = accountUseCases;
        this.subscriptionUseCases = subscriptionUseCases;
        this.subscriptionChargeUseCases = subscriptionChargeUseCases;
    }

    @Override
    public Health health() {
        try {
            YearMonth currentPeriod = YearMonth.now();
            int activeAccounts = accountUseCases.listarActivas().size();
            int activeSubscriptions = subscriptionUseCases.listarActivas().size();
            int chargesInCurrentPeriod = subscriptionChargeUseCases
                    .listarPorMes(currentPeriod.getMonthValue(), currentPeriod.getYear())
                    .size();

            return Health.up()
                    .withDetail("currentPeriod", currentPeriod.toString())
                    .withDetail("activeAccounts", activeAccounts)
                    .withDetail("activeSubscriptions", activeSubscriptions)
                    .withDetail("chargesInCurrentPeriod", chargesInCurrentPeriod)
                    .build();
        } catch (Exception ex) {
            return Health.down(ex)
                    .withDetail("check", "Business read models query failed")
                    .build();
        }
    }
}
