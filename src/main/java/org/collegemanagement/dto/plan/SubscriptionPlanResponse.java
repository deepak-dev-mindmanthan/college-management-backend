package org.collegemanagement.dto.plan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.CurrencyCode;
import org.collegemanagement.enums.SubscriptionPlanType;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionPlanResponse {
    private Long id;
    private SubscriptionPlanType planType;
    private BillingCycle billingCycle;
    private BigDecimal price;
    private CurrencyCode currency;
    private boolean active;
}

