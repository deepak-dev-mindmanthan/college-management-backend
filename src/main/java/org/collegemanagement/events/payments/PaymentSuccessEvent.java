package org.collegemanagement.events.payments;


import lombok.Getter;
import org.collegemanagement.entity.finance.Payment;

/**
 * Domain Event: Published when a payment succeeds.
 *
 * This event is used to decouple payment logic from
 * post-payment business actions (subscription activation, notifications, etc.).
 */
@Getter
public class PaymentSuccessEvent {

    private final Payment payment;

    public PaymentSuccessEvent(Payment payment) {
        this.payment = payment;
    }
}
