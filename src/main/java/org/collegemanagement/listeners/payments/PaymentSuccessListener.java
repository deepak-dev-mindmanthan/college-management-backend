package org.collegemanagement.listeners.payments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.events.payments.PaymentSuccessEvent;
import org.collegemanagement.handlers.payments.PaymentSuccessHandler;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Recover;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Handles post-payment success actions.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessListener {

    private final PaymentSuccessHandler handler;

    @Async
    @EventListener
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        log.info("PaymentSuccessEvent received (async)");
        handler.handle(event);
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
