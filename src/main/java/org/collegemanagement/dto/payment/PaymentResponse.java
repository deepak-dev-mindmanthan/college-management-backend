package org.collegemanagement.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.enums.PaymentGateway;
import org.collegemanagement.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private String uuid;
    private String invoiceUuid;
    private String invoiceNumber;
    private PaymentGateway gateway;
    private String transactionId;
    private BigDecimal amount;
    private PaymentStatus status;
    private Instant paymentDate;
    private Instant createdAt;
    private Instant updatedAt;
}

