package org.collegemanagement.listeners.payments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.events.payments.PaymentFailedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.collegemanagement.handlers.payments.PaymentFailureHandler;

/**
 * Async listener for payment failure events.
 * Only delegates to retryable handler.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailureListener {

    private final PaymentFailureHandler handler;

    @Async
    @EventListener
    public void onPaymentFailure(PaymentFailedEvent event) {
        log.info(
                "PaymentFailedEvent received | payment={} | tenant={}",
                event.paymentUuid(),
                event.tenantId()
        );
        handler.handle(event);
    }
}

