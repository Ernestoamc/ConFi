package com.confi.domain.service;

import com.confi.domain.model.DomainEvent;
import com.confi.domain.port.out.DomainEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@Service
public class PeriodCloseService {

    private final Set<YearMonth> closedPeriods = new TreeSet<>();
    private final List<PeriodEventLog> eventHistory = new ArrayList<>();
    private final DomainEventPublisher domainEventPublisher;

    public PeriodCloseService(DomainEventPublisher domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }

    public synchronized void close(YearMonth period) {
        if (period == null) {
            throw new IllegalArgumentException("period es obligatorio");
        }
        boolean changed = closedPeriods.add(period);
        if (changed) {
            publishPeriodEvent("period.closed", period);
        } else {
            publishPeriodRejectedEvent("period.close.rejected", period, "already-closed");
        }
    }

    public synchronized void reopen(YearMonth period) {
        if (period == null) {
            throw new IllegalArgumentException("period es obligatorio");
        }
        boolean changed = closedPeriods.remove(period);
        if (changed) {
            publishPeriodEvent("period.reopened", period);
        } else {
            publishPeriodRejectedEvent("period.reopen.rejected", period, "not-closed");
        }
    }

    public synchronized boolean isClosed(YearMonth period) {
        return closedPeriods.contains(period);
    }

    public synchronized Set<YearMonth> listClosed() {
        return Set.copyOf(closedPeriods);
    }

    public synchronized int restoreClosed(Set<YearMonth> periods) {
        closedPeriods.clear();
        closedPeriods.addAll(periods);
        return closedPeriods.size();
    }

    public synchronized List<PeriodEventLog> listEvents(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 500));
        List<PeriodEventLog> result = new ArrayList<>();
        for (int i = eventHistory.size() - 1; i >= 0 && result.size() < safeLimit; i--) {
            result.add(eventHistory.get(i));
        }
        return result;
    }

    public synchronized List<PeriodEventLog> snapshotEvents() {
        return List.copyOf(eventHistory);
    }

    public synchronized int restoreEvents(List<PeriodEventLog> events) {
        eventHistory.clear();
        if (events != null) {
            eventHistory.addAll(events);
        }
        return eventHistory.size();
    }

    public void ensureOpen(Instant instant, String context) {
        YearMonth period = YearMonth.from(instant.atZone(ZoneOffset.UTC));
        if (isClosed(period)) {
            throw new IllegalStateException("El periodo " + period + " esta cerrado para " + context);
        }
    }

    private void publishPeriodEvent(String eventType, YearMonth period) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("year", period.getYear());
        payload.put("month", period.getMonthValue());
        payload.put("period", period.toString());
        payload.put("changedAt", Instant.now().toString());
        DomainEvent event = DomainEvent.create(eventType, payload);
        appendEventHistory(event);
        domainEventPublisher.publish(event);
    }

    private void publishPeriodRejectedEvent(String eventType, YearMonth period, String reason) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("year", period.getYear());
        payload.put("month", period.getMonthValue());
        payload.put("period", period.toString());
        payload.put("reason", reason);
        payload.put("attemptedAt", Instant.now().toString());
        DomainEvent event = DomainEvent.create(eventType, payload);
        appendEventHistory(event);
        domainEventPublisher.publish(event);
    }

    private void appendEventHistory(DomainEvent event) {
        Object yearRaw = event.payload().get("year");
        Object monthRaw = event.payload().get("month");
        Object periodRaw = event.payload().get("period");
        Object reasonRaw = event.payload().get("reason");

        Integer year = yearRaw instanceof Number number ? number.intValue() : null;
        Integer month = monthRaw instanceof Number number ? number.intValue() : null;
        String period = periodRaw != null ? String.valueOf(periodRaw) : null;
        String reason = reasonRaw != null ? String.valueOf(reasonRaw) : null;

        eventHistory.add(new PeriodEventLog(
                event.eventType(),
                period,
                year,
                month,
                reason,
                event.occurredAt()
        ));
    }

    public record PeriodEventLog(
            String eventType,
            String period,
            Integer year,
            Integer month,
            String reason,
            Instant occurredAt
    ) {
    }
}
