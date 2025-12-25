package org.collegemanagement.dto.payment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.enums.PaymentStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessPaymentRequest {

    @NotBlank(message = "Payment UUID is required")
    private String paymentUuid;

    @NotNull(message = "Payment status is required")
    private PaymentStatus status;

    private String gatewayResponseId; // Gateway's response/confirmation ID
    private String failureReason; // Reason if payment failed
}

