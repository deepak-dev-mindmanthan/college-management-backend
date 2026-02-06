package org.collegemanagement.events.payments;


/**
 * Domain Event: Published when a payment fails.
 */

public record PaymentFailedEvent(
        String paymentUuid,
        Long tenantId,
        String collegeEmail,
        String collegeName,
        String invoiceNumber,
        String reason
) {}