package org.collegemanagement.listeners.payments;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.entity.finance.Invoice;
import org.collegemanagement.events.payments.PaymentFailedEvent;
import org.collegemanagement.services.EmailService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener: Handles actions after failed payments.
 *
 * Responsibility:
 * Send failure notification email
 *
 * SOLID:
 * - SRP: Only failure notification
 * - OCP: More failure listeners can be added without modifying PaymentService
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentFailureListener {

    private final EmailService emailService;

    @EventListener
    @Async
    public void onPaymentFailure(PaymentFailedEvent event) {

        Invoice invoice = event.getPayment().getInvoice();
        String reason = event.getReason();

        log.warn("Payment failed for Invoice {} | Reason: {}",
                invoice.getInvoiceNumber(), reason);

        emailService.sendPaymentFailureEmail(
                invoice.getCollege().getEmail(),
                invoice.getCollege().getName(),
                invoice.getInvoiceNumber(),
                reason != null ? reason : "Payment failed"
        );
    }
}

