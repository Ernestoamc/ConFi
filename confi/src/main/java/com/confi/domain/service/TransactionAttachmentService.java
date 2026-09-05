package com.confi.domain.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class TransactionAttachmentService {

    private final CopyOnWriteArrayList<Attachment> attachments = new CopyOnWriteArrayList<>();

    public Attachment add(UUID transactionId, String fileName, String contentType, String url) {
        if (transactionId == null) {
            throw new IllegalArgumentException("transactionId es obligatorio");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("fileName es obligatorio");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url es obligatorio");
        }

        Attachment attachment = new Attachment(
                UUID.randomUUID(),
                transactionId,
                fileName.trim(),
                contentType == null ? "application/octet-stream" : contentType,
                url.trim(),
                Instant.now()
        );
        attachments.add(attachment);
        return attachment;
    }

    public List<Attachment> listByTransaction(UUID transactionId) {
        return attachments.stream().filter(a -> a.transactionId().equals(transactionId)).toList();
    }

    public List<Attachment> listAll() {
        return new ArrayList<>(attachments);
    }

    public int restore(List<Attachment> restored) {
        attachments.clear();
        attachments.addAll(restored);
        return attachments.size();
    }

    public record Attachment(
            UUID id,
            UUID transactionId,
            String fileName,
            String contentType,
            String url,
            Instant uploadedAt
    ) {
    }
}
