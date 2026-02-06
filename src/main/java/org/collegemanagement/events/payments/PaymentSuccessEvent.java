package org.collegemanagement.events.payments;


/**
 * Domain Event: Published when a payment succeeds.
 *
 * This event is used to decouple payment logic from
 * post-payment business actions (subscription activation, notifications, etc.).
 */
public record PaymentSuccessEvent(
        String subscriptionUuid,
        String paymentUuid,
        Long tenantId
) {}