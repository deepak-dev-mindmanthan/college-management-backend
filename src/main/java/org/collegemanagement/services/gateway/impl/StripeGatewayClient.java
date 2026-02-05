package org.collegemanagement.services.gateway.impl;


import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.enums.PaymentGateway;
import org.collegemanagement.services.gateway.PaymentGatewayClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("stripeClient")
@Slf4j
public class StripeGatewayClient implements PaymentGatewayClient {

    @Override
    public PaymentGateway getGateway() {
        return PaymentGateway.STRIPE;
    }

    @Override
    public String createOrder(String receipt, BigDecimal amount) {

        // TODO: Stripe PaymentIntent creation
        String paymentIntentId = "order_gw_txn_id_from_payload";;

        log.info("Stripe payment intent created: {}", paymentIntentId);
        return paymentIntentId;
    }

    @Override
    public boolean verifySignature(String payload, String signature) {
        // TODO: Stripe signature verification
        return true;
    }
}

