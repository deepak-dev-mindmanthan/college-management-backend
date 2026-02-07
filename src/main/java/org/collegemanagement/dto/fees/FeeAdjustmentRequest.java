package org.collegemanagement.dto.fees;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.AdjustmentType;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeAdjustmentRequest {

    @NotNull(message = "Adjustment type is required")
    private AdjustmentType type;

    @NotNull(message = "Adjustment amount is required")
    @Positive(message = "Adjustment amount must be positive")
    private BigDecimal amount;

    private String reason;
}
