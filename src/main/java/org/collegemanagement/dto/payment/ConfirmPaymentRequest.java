package org.collegemanagement.dto.payment;

import lombok.*;
import org.collegemanagement.enums.PaymentGateway;
import org.collegemanagement.enums.PaymentStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmPaymentRequest {
    private String gatewayOrderId;
    private String gatewayTransactionId;
    private PaymentStatus status;
    private PaymentGateway gateway;
    private String failureReason;
}
