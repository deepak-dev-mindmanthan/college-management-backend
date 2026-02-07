package org.collegemanagement.dto.fees;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.AdjustmentType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeAdjustmentResponse {

    private String uuid;
    private String studentFeeUuid;
    private AdjustmentType type;
    private BigDecimal amount;
    private String reason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
