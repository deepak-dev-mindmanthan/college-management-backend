package org.collegemanagement.services.gateway;

import org.collegemanagement.enums.PaymentGateway;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class PaymentGatewayFactory {

    private final Map<PaymentGateway, PaymentGatewayClient> gatewayClients;

    public PaymentGatewayFactory(List<PaymentGatewayClient> clients) {

        this.gatewayClients = new EnumMap<>(PaymentGateway.class);

        for (PaymentGatewayClient client : clients) {
            gatewayClients.put(client.getGateway(), client);
        }
    }

    public PaymentGatewayClient getClient(PaymentGateway gateway) {

        PaymentGatewayClient client = gatewayClients.get(gateway);

        if (client == null) {
            throw new IllegalArgumentException(
                    "No PaymentGatewayClient found for gateway: " + gateway
            );
        }

        return client;
    }
}
