package org.collegemanagement.services.gateway.impl;


import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.enums.PaymentGateway;
import org.collegemanagement.services.gateway.PaymentGatewayClient;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component("razorpayClient")
@Slf4j
public class RazorpayGatewayClient implements PaymentGatewayClient {


    @Override
    public PaymentGateway getGateway() {
        return PaymentGateway.RAZORPAY;
    }

    @Override
    public String createOrder(String receipt, BigDecimal amount) {

        // TODO: Replace with Razorpay SDK call
        // RazorpayOrder order = razorpay.orders.create(...)

        String mockOrderId = "order_" + "gw_order_id_from_payload";

        log.info("Razorpay order created: {}", mockOrderId);
        return mockOrderId;
    }

    @Override
    public boolean verifySignature(String payload, String signature) {
        // TODO: Razorpay webhook signature verification
        return true;
    }
}

