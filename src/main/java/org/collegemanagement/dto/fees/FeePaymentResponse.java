package org.collegemanagement.dto.fees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.PaymentMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeePaymentResponse {

    private String uuid;
    private String studentFeeUuid;
    private String studentUuid;
    private String studentName;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private String transactionId;
    private Instant paymentDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

