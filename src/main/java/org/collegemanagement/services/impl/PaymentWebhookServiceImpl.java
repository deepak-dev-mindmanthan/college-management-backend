package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.payment.ConfirmPaymentRequest;
import org.collegemanagement.enums.PaymentGateway;
import org.collegemanagement.enums.PaymentStatus;
import org.collegemanagement.services.PaymentService;
import org.collegemanagement.services.PaymentWebhookService;
import org.collegemanagement.services.gateway.PaymentGatewayClient;
import org.collegemanagement.services.gateway.PaymentGatewayFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentWebhookServiceImpl implements PaymentWebhookService {

    private final PaymentGatewayFactory paymentGatewayFactory;
    private final PaymentService paymentService;

    @Override
    public void handleRazorpay(String payload, String signature) {

        PaymentGatewayClient paymentGatewayClient = paymentGatewayFactory.getClient(PaymentGateway.RAZORPAY);

        if (!paymentGatewayClient.verifySignature(payload, signature)) {
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

        PaymentGatewayClient paymentGatewayClient = paymentGatewayFactory.getClient(PaymentGateway.STRIPE);

        if (!paymentGatewayClient.verifySignature(payload, signature)) {
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
        return "order_gw_order_id_from_payload";
    }

    private String extractTransactionId(String payload) {
        return "transaction_gw_txn_id_from_payload";
    }
}
