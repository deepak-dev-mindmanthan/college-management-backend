package org.collegemanagement.dto.fees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassFeeSummaryResponse {

    private String classUuid;
    private String className;
    private String section;
    private BigDecimal totalFees;
    private BigDecimal totalPaid;
    private BigDecimal totalDue;
    private Long totalStudents;
    private Long pendingCount;
    private Long paidCount;
    private Long partiallyPaidCount;
    private Long overdueCount;
}

