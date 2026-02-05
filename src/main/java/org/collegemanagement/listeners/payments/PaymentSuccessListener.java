package org.collegemanagement.listeners.payments;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.entity.finance.Invoice;
import org.collegemanagement.entity.subscription.Subscription;
import org.collegemanagement.enums.SubscriptionStatus;
import org.collegemanagement.events.payments.PaymentSuccessEvent;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Listener: Handles business processes after successful payment.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSuccessListener {

    private final SubscriptionService subscriptionService;

    @EventListener
    @Async
    public void onPaymentSuccess(PaymentSuccessEvent event) {

        Invoice invoice = event.getPayment().getInvoice();
        Subscription subscription = invoice.getSubscription();

        // If payment is not for subscription, ignore
        if (subscription == null) {
            return;
        }

        // Activate only pending subscriptions
        if (subscription.getStatus() != SubscriptionStatus.PENDING) {
            log.info("Subscription {} already processed, skipping activation",
                    subscription.getUuid());
            return;
        }

        log.info("Payment succeeded → Activating subscription {}",
                subscription.getUuid());

        // Webhook-safe internal method
        subscriptionService.activateSubscriptionFromSystem(subscription.getUuid(),invoice.getCollege().getId());
    }
}
