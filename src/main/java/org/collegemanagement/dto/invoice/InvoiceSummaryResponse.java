package org.collegemanagement.dto.invoice;

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
public class InvoiceSummaryResponse {
    private Long totalInvoices;
    private Long paidInvoices;
    private Long unpaidInvoices;
    private Long failedInvoices;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal unpaidAmount;
    private BigDecimal failedAmount;
}

