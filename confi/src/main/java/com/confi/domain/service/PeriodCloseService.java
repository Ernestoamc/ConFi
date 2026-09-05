package com.confi.domain.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Set;
import java.util.TreeSet;

@Service
public class PeriodCloseService {

    private final Set<YearMonth> closedPeriods = new TreeSet<>();

    public synchronized void close(YearMonth period) {
        if (period == null) {
            throw new IllegalArgumentException("period es obligatorio");
        }
        closedPeriods.add(period);
    }

    public synchronized void reopen(YearMonth period) {
        if (period == null) {
            throw new IllegalArgumentException("period es obligatorio");
        }
        closedPeriods.remove(period);
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

    public void ensureOpen(Instant instant, String context) {
        YearMonth period = YearMonth.from(instant.atZone(ZoneOffset.UTC));
        if (isClosed(period)) {
            throw new IllegalStateException("El periodo " + period + " esta cerrado para " + context);
        }
    }
}
