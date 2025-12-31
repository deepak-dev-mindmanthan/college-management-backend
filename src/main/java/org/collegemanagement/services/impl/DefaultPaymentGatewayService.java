package org.collegemanagement.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.entity.finance.Payment;
import org.collegemanagement.enums.PaymentGateway;
import org.collegemanagement.services.PaymentGatewayService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Slf4j
public class DefaultPaymentGatewayService implements PaymentGatewayService {

    /**
     * Simulate gateway order creation.
     * In real gateways:
     *  - Razorpay → order_id
     *  - Stripe   → payment_intent_id
     */
    @Override
    public String createOrder(Payment payment, BigDecimal amount) {

        String gatewayOrderId = "GW_ORDER_" + UUID.randomUUID();

        log.info(
                "[SIMULATION] Created gateway order | paymentUuid={} | amount={} | gatewayOrderId={}",
                payment.getUuid(),
                amount,
                gatewayOrderId
        );

        // In real integration, this value comes from gateway API
        return gatewayOrderId;
    }

    /**
     * Simulate webhook signature verification.
     * Always returns true in DEV mode.
     */
    @Override
    public boolean verifyWebhookSignature(
            PaymentGateway gateway,
            String payload,
            String signature
    ) {

        log.info(
                "[SIMULATION] Verifying webhook signature | gateway={} | signature={}",
                gateway,
                signature
        );

        // ALWAYS TRUST in simulation mode
        return true;
    }
}

