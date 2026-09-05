package com.confi.adapter.in.notifications;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class KafkaDomainEventListenerTest {

    private final NotificationInbox inbox = mock(NotificationInbox.class);
        private final KafkaDomainEventListener listener = new KafkaDomainEventListener(inbox);

    @Test
        void guardaNotificacionCuandoRecibeEventoValido() {
                String raw = """
                                {
                                    "eventId": "2eb8f2d7-c56a-4a39-a79a-b8ef3af8f1a2",
                                    "eventType": "subscription.charge.generated",
                                    "eventVersion": 1,
                                    "occurredAt": "2026-08-28T12:00:00Z",
                                    "payload": {
                                        "amount": "249.00",
                                        "dueDate": "2026-09-15"
                                    }
                                }
                                """;
        listener.onMessage(raw);

        verify(inbox).add(any(NotificationItem.class));
    }

    @Test
    void ignoraMensajeInvalidoSinRomperFlujo() {
        listener.onMessage("no-es-json");
        verify(inbox, never()).add(any(NotificationItem.class));
    }

        @Test
        void transformaEventoDeSaldoBajoEnNotificacionLegible() {
                String raw = """
                                {
                                    "eventId": "f1e62e3e-8e43-4e7c-b49e-2b5de96a0a8b",
                                    "eventType": "account.low.balance",
                                    "eventVersion": 1,
                                    "occurredAt": "2026-08-29T10:00:00Z",
                                    "payload": {
                                        "accountName": "BBVA Debito",
                                        "balance": "450.00",
                                        "threshold": "500.00"
                                    }
                                }
                                """;

                listener.onMessage(raw);

                ArgumentCaptor<NotificationItem> captor = ArgumentCaptor.forClass(NotificationItem.class);
                verify(inbox).add(captor.capture());
                NotificationItem item = captor.getValue();

                assertThat(item.title()).isEqualTo("Alerta de saldo bajo");
                assertThat(item.message()).contains("BBVA Debito").contains("450.00").contains("500.00");
        }
}
