package org.collegemanagement.handlers.payments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.events.payments.PaymentSuccessEvent;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessHandler {

    private final SubscriptionService subscriptionService;

    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 5000)
    )
    public void handle(PaymentSuccessEvent event) {

        log.info("Handling payment success (retryable)");

        subscriptionService.activateSubscriptionFromSystem(
                event.subscriptionUuid(),
                event.tenantId()
        );
    }

    @Recover
    public void recover(Exception ex, PaymentSuccessEvent event) {
        log.error(
                "PaymentSuccessEvent permanently failed | payment={} | tenant={}",
                event.paymentUuid(),
                event.tenantId(),
                ex
        );
    }
}

