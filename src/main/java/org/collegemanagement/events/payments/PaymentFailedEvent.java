package org.collegemanagement.events.payments;


import lombok.Getter;
import org.collegemanagement.entity.finance.Payment;

/**
 * Domain Event: Published when a payment fails.
 */
@Getter
public class PaymentFailedEvent {

    private final Payment payment;
    private final String reason;

    public PaymentFailedEvent(Payment payment, String reason) {
        this.payment = payment;
        this.reason = reason;
    }
}
