package org.collegemanagement.dto.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.enums.InvoiceStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponse {
    private String uuid;
    private String invoiceNumber;
    private String subscriptionUuid;
    private String planName;
    private BillingCycle billingCycle;
    private BigDecimal amount;
    private String currency;
    private InvoiceStatus status;
    private LocalDate dueDate;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;
    private Long paymentCount;
    private BigDecimal totalPaidAmount;

    public enum BillingCycle {
        MONTHLY, QUARTERLY, YEARLY
    }
}

