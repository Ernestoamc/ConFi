package com.confi.adapter.in.web;

import com.confi.adapter.in.notifications.NotificationInbox;
import com.confi.adapter.in.notifications.NotificationItem;
import com.confi.domain.service.CategorizationRuleService;
import com.confi.domain.service.PeriodCloseService;
import com.confi.domain.service.SavingsGoalService;
import com.confi.domain.service.TransactionAttachmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class BackupController {

    private final NotificationInbox notificationInbox;
        private final CategorizationRuleService categorizationRuleService;
        private final SavingsGoalService savingsGoalService;
        private final TransactionAttachmentService attachmentService;
        private final PeriodCloseService periodCloseService;

        public BackupController(NotificationInbox notificationInbox,
                                                        CategorizationRuleService categorizationRuleService,
                                                        SavingsGoalService savingsGoalService,
                                                        TransactionAttachmentService attachmentService,
                                                        PeriodCloseService periodCloseService) {
        this.notificationInbox = notificationInbox;
                this.categorizationRuleService = categorizationRuleService;
                this.savingsGoalService = savingsGoalService;
                this.attachmentService = attachmentService;
                this.periodCloseService = periodCloseService;
    }

    @GetMapping("/backups/notifications")
    public NotificationsBackupResponse backupNotifications() {
        List<NotificationItemResponse> items = notificationInbox.snapshot().stream()
                .map(BackupController::toResponse)
                .toList();
        return new NotificationsBackupResponse(Instant.now(), items);
    }

    @PostMapping("/restores/notifications")
    @ResponseStatus(HttpStatus.OK)
    public RestoreResponse restoreNotifications(@Valid @RequestBody RestoreNotificationsRequest request) {
        List<NotificationItem> restored = request.items().stream()
                .map(BackupController::toDomain)
                .toList();
        int count = notificationInbox.restore(restored);
        return new RestoreResponse(count);
    }

    @GetMapping("/backups/system")
    public SystemBackupResponse backupSystem() {
        List<NotificationItemResponse> notifications = notificationInbox.snapshot().stream()
                .map(BackupController::toResponse)
                .toList();

        List<RuleResponse> rules = categorizationRuleService.snapshot().stream()
                .map(rule -> new RuleResponse(rule.id(), rule.keyword(), rule.categoriaId(), rule.priority(), rule.active()))
                .toList();

        List<SavingsGoalResponse> goals = savingsGoalService.snapshot().stream()
                .map(goal -> new SavingsGoalResponse(
                        goal.id(), goal.name(), goal.targetAmount(), goal.currentAmount(), goal.targetDate(), goal.active()))
                .toList();

        List<AttachmentResponse> attachments = attachmentService.listAll().stream()
                .map(att -> new AttachmentResponse(att.id(), att.transactionId(), att.fileName(), att.contentType(), att.url(), att.uploadedAt()))
                .toList();

        List<String> closedPeriods = periodCloseService.listClosed().stream().map(YearMonth::toString).toList();

        return new SystemBackupResponse(Instant.now(), notifications, rules, goals, attachments, closedPeriods);
    }

    @PostMapping("/restores/system")
    public SystemRestoreResponse restoreSystem(@Valid @RequestBody SystemRestoreRequest request) {
        int notifications = notificationInbox.restore(request.notifications().stream().map(BackupController::toDomain).toList());
        int rules = categorizationRuleService.restore(request.rules().stream()
                .map(r -> new CategorizationRuleService.Rule(r.id(), r.keyword(), r.categoriaId(), r.priority(), r.active()))
                .toList());
        int goals = savingsGoalService.restore(request.goals().stream()
                .map(g -> new SavingsGoalService.SavingsGoal(g.id(), g.name(), g.targetAmount(), g.currentAmount(), g.targetDate(), g.active()))
                .toList());
        int attachments = attachmentService.restore(request.attachments().stream()
                .map(a -> new TransactionAttachmentService.Attachment(a.id(), a.transactionId(), a.fileName(), a.contentType(), a.url(), a.uploadedAt()))
                .toList());
        int closedPeriods = periodCloseService.restoreClosed(request.closedPeriods().stream().map(YearMonth::parse).collect(java.util.stream.Collectors.toSet()));

        return new SystemRestoreResponse(notifications, rules, goals, attachments, closedPeriods);
    }

    private static NotificationItemResponse toResponse(NotificationItem item) {
        return new NotificationItemResponse(
                item.id(),
                item.eventType(),
                item.occurredAt(),
                item.title(),
                item.message(),
                item.payload(),
                item.read(),
                item.readAt()
        );
    }

    private static NotificationItem toDomain(NotificationItemRequest item) {
        return new NotificationItem(
                item.id(),
                item.eventType(),
                item.occurredAt(),
                item.title(),
                item.message(),
                item.payload(),
                item.read(),
                item.readAt()
        );
    }

    public record NotificationsBackupResponse(
            Instant generatedAt,
            List<NotificationItemResponse> items
    ) {
    }

    public record RestoreNotificationsRequest(
            @NotNull List<NotificationItemRequest> items
    ) {
    }

    public record RestoreResponse(int restoredCount) {
    }

    public record SystemBackupResponse(
            Instant generatedAt,
            List<NotificationItemResponse> notifications,
            List<RuleResponse> rules,
            List<SavingsGoalResponse> goals,
            List<AttachmentResponse> attachments,
            List<String> closedPeriods
    ) {
    }

    public record SystemRestoreRequest(
            @NotNull List<NotificationItemRequest> notifications,
            @NotNull List<RuleResponse> rules,
            @NotNull List<SavingsGoalResponse> goals,
            @NotNull List<AttachmentResponse> attachments,
            @NotNull List<String> closedPeriods
    ) {
    }

    public record SystemRestoreResponse(
            int notifications,
            int rules,
            int goals,
            int attachments,
            int closedPeriods
    ) {
    }

    public record NotificationItemRequest(
            @NotNull UUID id,
            @NotNull String eventType,
            @NotNull Instant occurredAt,
            @NotNull String title,
            @NotNull String message,
            @NotNull Map<String, Object> payload,
            boolean read,
            Instant readAt
    ) {
    }

    public record NotificationItemResponse(
            UUID id,
            String eventType,
            Instant occurredAt,
            String title,
            String message,
            Map<String, Object> payload,
            boolean read,
            Instant readAt
    ) {
    }

    public record RuleResponse(UUID id, String keyword, UUID categoriaId, int priority, boolean active) {
    }

    public record SavingsGoalResponse(
            UUID id,
            String name,
            java.math.BigDecimal targetAmount,
            java.math.BigDecimal currentAmount,
            Instant targetDate,
            boolean active
    ) {
    }

    public record AttachmentResponse(
            UUID id,
            UUID transactionId,
            String fileName,
            String contentType,
            String url,
            Instant uploadedAt
    ) {
    }
}
