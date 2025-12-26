package org.collegemanagement.dto.fees;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeeComponentRequest {

    @NotBlank(message = "Component name is required")
    private String name;

    @NotNull(message = "Component amount is required")
    @Positive(message = "Component amount must be positive")
    private BigDecimal amount;
}

