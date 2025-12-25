package org.collegemanagement.services;

import org.collegemanagement.entity.finance.Payment;
import org.collegemanagement.enums.PaymentGateway;
import org.collegemanagement.enums.PaymentStatus;

import java.math.BigDecimal;

/**
 * Service interface for payment gateway integration.
 * Follows Strategy pattern to support multiple payment gateways.
 * 
 * TODO: Integrate payment gateway
 * - Implement actual gateway API calls
 * - Handle gateway-specific request/response formats
 * - Implement webhook handling for payment status updates
 * - Add retry logic for failed requests
 * - Implement idempotency checks
 */
public interface PaymentGatewayService {

    /**
     * Process payment through the gateway.
     * 
     * @param payment The payment entity to process
     * @param gateway The payment gateway to use
     * @param amount The payment amount
     * @param transactionId The transaction ID
     * @return PaymentStatus result (SUCCESS, FAILED, or PENDING)
     * 
     * TODO: Integrate payment gateway
     * - Make actual API call to payment gateway
     * - Handle gateway response
     * - Map gateway response to PaymentStatus
     * - Store gateway response details for audit
     */
    PaymentStatus processPayment(Payment payment, PaymentGateway gateway, BigDecimal amount, String transactionId);

    /**
     * Verify payment status with the gateway.
     * Useful for checking payment status via webhook or polling.
     * 
     * @param payment The payment entity to verify
     * @param gateway The payment gateway used
     * @param transactionId The transaction ID from gateway
     * @return PaymentStatus result
     * 
     * TODO: Integrate payment gateway
     * - Implement gateway status verification API call
     * - Handle gateway-specific status codes
     * - Map gateway status to PaymentStatus enum
     */
    PaymentStatus verifyPaymentStatus(Payment payment, PaymentGateway gateway, String transactionId);

    /**
     * Check if the gateway supports the given payment gateway type.
     * 
     * @param gateway The payment gateway type
     * @return true if supported, false otherwise
     */
    boolean supports(PaymentGateway gateway);
}

