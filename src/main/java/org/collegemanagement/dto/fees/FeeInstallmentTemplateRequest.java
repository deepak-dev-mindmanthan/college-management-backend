package org.collegemanagement.dto.fees;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeInstallmentTemplateRequest {

    @NotBlank(message = "Installment name is required")
    private String name;

    @NotNull(message = "Installment amount is required")
    @Positive(message = "Installment amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "Installment due date is required")
    private LocalDate dueDate;
}
