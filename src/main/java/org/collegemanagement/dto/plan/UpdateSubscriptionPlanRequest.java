package org.collegemanagement.dto.plan;

import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.enums.CurrencyCode;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSubscriptionPlanRequest {

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private CurrencyCode currency;
    private Boolean active;
}

