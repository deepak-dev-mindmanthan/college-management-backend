package org.collegemanagement.dto.fees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentFeeSummaryResponse {

    private String studentUuid;
    private String studentName;
    private String rollNumber;
    private String className;
    private BigDecimal totalFees;
    private BigDecimal totalPaid;
    private BigDecimal totalDue;
    private Long pendingCount;
    private Long paidCount;
    private Long partiallyPaidCount;
    private Long overdueCount;
    private List<StudentFeeResponse> fees;
}

