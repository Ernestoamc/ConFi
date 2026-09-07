package com.confi.adapter.in.notifications;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;

@Component
public class NotificationInbox {

    private static final int MAX_ITEMS = 200;

    private final List<NotificationItem> items = new ArrayList<>();

    public synchronized void add(NotificationItem item) {
        items.add(0, item);
        while (items.size() > MAX_ITEMS) {
            items.remove(items.size() - 1);
        }
    }

    public synchronized List<NotificationItem> latest(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, MAX_ITEMS));
        int end = Math.min(safeLimit, items.size());
        return new ArrayList<>(items.subList(0, end));
    }

    public synchronized NotificationItem markRead(UUID id) {
        for (int i = 0; i < items.size(); i++) {
            NotificationItem current = items.get(i);
            if (current.id().equals(id)) {
                NotificationItem updated = current.markAsRead(Instant.now());
                items.set(i, updated);
                return updated;
            }
        }
        throw new NoSuchElementException("Notificacion no encontrada: " + id);
    }

    public synchronized int markAllRead() {
        int changed = 0;
        Instant now = Instant.now();
        for (int i = 0; i < items.size(); i++) {
            NotificationItem current = items.get(i);
            if (!current.read()) {
                items.set(i, current.markAsRead(now));
                changed++;
            }
        }
        return changed;
    }

    public synchronized long unreadCount() {
        return items.stream().filter(item -> !item.read()).count();
    }

    public synchronized List<NotificationItem> snapshot() {
        return new ArrayList<>(items);
    }

    public synchronized int restore(List<NotificationItem> restoredItems) {
        items.clear();
        for (NotificationItem item : restoredItems) {
            items.add(item);
            if (items.size() >= MAX_ITEMS) {
                break;
            }
        }
        return items.size();
    }

    public synchronized void clear() {
        items.clear();
    }

    public synchronized ReminderMetrics reminderMetrics() {
        Set<String> remindedCharges = new HashSet<>();
        Set<String> confirmedCharges = new HashSet<>();
        Set<String> skippedCharges = new HashSet<>();

        for (NotificationItem item : items) {
            String eventType = item.eventType();
            Object chargeIdRaw = item.payload().get("chargeId");
            if (chargeIdRaw == null) {
                continue;
            }
            String chargeId = String.valueOf(chargeIdRaw);

            if ("subscription.charge.due.soon".equals(eventType)) {
                remindedCharges.add(chargeId);
            } else if ("subscription.charge.confirmed".equals(eventType)) {
                confirmedCharges.add(chargeId);
            } else if ("subscription.charge.skipped".equals(eventType)) {
                skippedCharges.add(chargeId);
            }
        }

        long reminded = remindedCharges.size();
        long confirmedAfterReminder = confirmedCharges.stream().filter(remindedCharges::contains).count();
        long skippedAfterReminder = skippedCharges.stream().filter(remindedCharges::contains).count();
        long unresolved = Math.max(0, reminded - confirmedAfterReminder - skippedAfterReminder);
        double conversionRate = reminded > 0 ? (double) confirmedAfterReminder / (double) reminded : 0.0d;

        return new ReminderMetrics(reminded, confirmedAfterReminder, skippedAfterReminder, unresolved, conversionRate);
    }

    public record ReminderMetrics(
            long reminded,
            long confirmedAfterReminder,
            long skippedAfterReminder,
            long unresolved,
            double conversionRate
    ) {
    }
}
