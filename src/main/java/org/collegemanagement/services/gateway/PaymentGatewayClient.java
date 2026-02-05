package org.collegemanagement.services.gateway;


import org.collegemanagement.enums.PaymentGateway;

import java.math.BigDecimal;

public interface PaymentGatewayClient {


    PaymentGateway getGateway();

    String createOrder(String receipt, BigDecimal amount);

    boolean verifySignature(String payload, String signature);
}

