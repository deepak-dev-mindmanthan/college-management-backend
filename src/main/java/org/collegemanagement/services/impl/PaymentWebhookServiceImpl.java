package org.collegemanagement.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.enums.PaymentGateway;
import org.collegemanagement.services.PaymentWebhookService;
import org.springframework.stereotype.Service;

/**
 * Default implementation of PaymentWebhookService.

 * This is a placeholder implementation for webhook handling.

 * TODO: Integrate payment gateway webhooks
 * Replace this implementation with actual webhook handlers:
 * - RazorpayWebhookService for RAZORPAY webhooks
 * - StripeWebhookService for STRIPE webhooks
 * - Use factory pattern to select appropriate handler based on gateway
 */

import lombok.RequiredArgsConstructor;
import org.collegemanagement.dto.payment.ConfirmPaymentRequest;
import org.collegemanagement.enums.PaymentStatus;
import org.collegemanagement.services.PaymentGatewayService;
import org.collegemanagement.services.PaymentService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private final PaymentGatewayService gatewayService;
    private final PaymentService paymentService;

    @Override
    public void handleRazorpay(String payload, String signature) {

        if (!gatewayService.verifyWebhookSignature(
                PaymentGateway.RAZORPAY, payload, signature)) {
            throw new SecurityException("Invalid Razorpay webhook signature");
        }

        // TODO: Proper JSON parsing (Razorpay SDK / Jackson)
        String gatewayOrderId = extractGatewayOrderId(payload);
        String gatewayTransactionId = extractTransactionId(payload);
        PaymentStatus status = PaymentStatus.SUCCESS;

        paymentService.confirmPayment(
                ConfirmPaymentRequest.builder()
                        .gateway(PaymentGateway.RAZORPAY)
                        .gatewayOrderId(gatewayOrderId)
                        .gatewayTransactionId(gatewayTransactionId)
                        .status(status)
                        .build()
        );
    }

    @Override
    public void handleStripe(String payload, String signature) {

        if (!gatewayService.verifyWebhookSignature(
                PaymentGateway.STRIPE, payload, signature)) {
            throw new SecurityException("Invalid Stripe webhook signature");
        }

        // TODO: Proper JSON parsing (Stripe SDK)
        String gatewayOrderId = extractGatewayOrderId(payload);
        String gatewayTransactionId = extractTransactionId(payload);
        PaymentStatus status = PaymentStatus.SUCCESS;

        paymentService.confirmPayment(
                ConfirmPaymentRequest.builder()
                        .gateway(PaymentGateway.STRIPE)
                        .gatewayOrderId(gatewayOrderId)
                        .gatewayTransactionId(gatewayTransactionId)
                        .status(status)
                        .build()
        );
    }

    // -----------------------
    // TEMP helpers (replace with real parsing)
    // -----------------------

    private String extractGatewayOrderId(String payload) {
        return "gw_order_id_from_payload";
    }

    private String extractTransactionId(String payload) {
        return "gw_txn_id_from_payload";
    }
}
