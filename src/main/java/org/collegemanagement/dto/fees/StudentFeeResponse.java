package org.collegemanagement.dto.fees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.FeeStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentFeeResponse {

    private String uuid;
    private String studentUuid;
    private String studentName;
    private String rollNumber;
    private String feeStructureUuid;
    private String className;
    private String section;
    private BigDecimal totalAmount;
    private BigDecimal netAmount;
    private BigDecimal discountAmount;
    private BigDecimal waiverAmount;
    private BigDecimal penaltyAmount;
    private BigDecimal paidAmount;
    private BigDecimal dueAmount;
    private LocalDate dueDate;
    private FeeStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

