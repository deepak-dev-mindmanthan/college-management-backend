package org.collegemanagement.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.collegemanagement.enums.PaymentGateway;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InitiatePaymentRequest {

    @NotBlank(message = "Invoice UUID is required")
    private String invoiceUuid;

    @NotNull(message = "Payment gateway is required")
    private PaymentGateway gateway;
}

