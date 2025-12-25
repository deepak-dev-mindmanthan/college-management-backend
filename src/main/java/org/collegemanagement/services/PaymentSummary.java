package org.collegemanagement.services;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSummary {
    private Long totalPayments;
    private Long successfulPayments;
    private Long pendingPayments;
    private Long failedPayments;
    private BigDecimal totalAmount;
    private BigDecimal successfulAmount;
    private BigDecimal pendingAmount;
    private BigDecimal failedAmount;
}

