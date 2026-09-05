package com.confi.adapter.in.notifications;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
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
}
