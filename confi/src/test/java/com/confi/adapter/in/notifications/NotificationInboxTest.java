package com.confi.adapter.in.notifications;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationInboxTest {

    @Test
    void calculaMetricasDeRecordatorios() {
        NotificationInbox inbox = new NotificationInbox();

        String chargeA = UUID.randomUUID().toString();
        String chargeB = UUID.randomUUID().toString();
        String chargeC = UUID.randomUUID().toString();

        inbox.add(new NotificationItem(
                UUID.randomUUID(),
                "subscription.charge.due.soon",
                Instant.parse("2026-09-01T10:00:00Z"),
                "Recordatorio",
                "A",
                Map.of("chargeId", chargeA),
                false,
                null
        ));

        inbox.add(new NotificationItem(
                UUID.randomUUID(),
                "subscription.charge.due.soon",
                Instant.parse("2026-09-01T10:01:00Z"),
                "Recordatorio",
                "B",
                Map.of("chargeId", chargeB),
                false,
                null
        ));

        inbox.add(new NotificationItem(
                UUID.randomUUID(),
                "subscription.charge.due.soon",
                Instant.parse("2026-09-01T10:02:00Z"),
                "Recordatorio",
                "C",
                Map.of("chargeId", chargeC),
                false,
                null
        ));

        inbox.add(new NotificationItem(
                UUID.randomUUID(),
                "subscription.charge.confirmed",
                Instant.parse("2026-09-01T12:00:00Z"),
                "Confirmado",
                "A",
                Map.of("chargeId", chargeA),
                false,
                null
        ));

        inbox.add(new NotificationItem(
                UUID.randomUUID(),
                "subscription.charge.skipped",
                Instant.parse("2026-09-01T12:05:00Z"),
                "Omitido",
                "B",
                Map.of("chargeId", chargeB),
                false,
                null
        ));

        NotificationInbox.ReminderMetrics metrics = inbox.reminderMetrics();

        assertThat(metrics.reminded()).isEqualTo(3);
        assertThat(metrics.confirmedAfterReminder()).isEqualTo(1);
        assertThat(metrics.skippedAfterReminder()).isEqualTo(1);
        assertThat(metrics.unresolved()).isEqualTo(1);
        assertThat(metrics.conversionRate()).isEqualTo(1.0 / 3.0);
    }
}
