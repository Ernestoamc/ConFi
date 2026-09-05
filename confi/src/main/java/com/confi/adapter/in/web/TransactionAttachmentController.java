package com.confi.adapter.in.web;

import com.confi.domain.service.TransactionAttachmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/transactions/{transactionId}/attachments")
public class TransactionAttachmentController {

    private final TransactionAttachmentService service;

    public TransactionAttachmentController(TransactionAttachmentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentResponse add(@PathVariable UUID transactionId,
                                  @Valid @RequestBody AddAttachmentRequest request) {
        return toResponse(service.add(transactionId, request.fileName(), request.contentType(), request.url()));
    }

    @GetMapping
    public List<AttachmentResponse> list(@PathVariable UUID transactionId) {
        return service.listByTransaction(transactionId).stream().map(TransactionAttachmentController::toResponse).toList();
    }

    private static AttachmentResponse toResponse(TransactionAttachmentService.Attachment attachment) {
        return new AttachmentResponse(
                attachment.id(),
                attachment.transactionId(),
                attachment.fileName(),
                attachment.contentType(),
                attachment.url(),
                attachment.uploadedAt()
        );
    }

    public record AddAttachmentRequest(
            @NotBlank String fileName,
            String contentType,
            @NotBlank String url
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
