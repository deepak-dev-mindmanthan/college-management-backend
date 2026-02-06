package org.collegemanagement.handlers.payments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.events.payments.PaymentFailedEvent;
import org.collegemanagement.services.EmailService;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

/**
 * Handles payment failure side-effects (retryable).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailureHandler {

    private final EmailService emailService;

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000)
    )
    public void handle(PaymentFailedEvent event) {

        log.info(
                "Handling payment failure | payment={} | tenant={}",
                event.paymentUuid(),
                event.tenantId()
        );

        emailService.sendPaymentFailureEmail(
                event.collegeEmail(),
                event.collegeName(),
                event.invoiceNumber(),
                event.reason() != null ? event.reason() : "Payment failed"
        );
    }

    /**
     * Called after retries are exhausted.
     */
    @Recover
    public void recover(Exception ex, PaymentFailedEvent event) {
        log.error(
                "PaymentFailedEvent permanently failed | payment={} | tenant={} | reason={}",
                event.paymentUuid(),
                event.tenantId(),
                event.reason(),
                ex
        );
        // Future: persist failure / notify ops / outbox fallback
    }
}
