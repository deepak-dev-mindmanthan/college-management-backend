package org.collegemanagement.services.gateway;


import java.math.BigDecimal;

public interface PaymentGatewayClient {

    String createOrder(String receipt, BigDecimal amount);

    boolean verifySignature(String payload, String signature);
}

