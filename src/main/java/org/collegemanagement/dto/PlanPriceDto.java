package org.collegemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.entity.PlanPrice;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlan;
import org.collegemanagement.enums.CurrencyCode;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanPriceDto {
    private Long id;
    private SubscriptionPlan plan;
    private BillingCycle billingCycle;
    private BigDecimal amount;
    private CurrencyCode currency;
    private boolean active;

    public static PlanPriceDto fromEntity(PlanPrice price) {
        return PlanPriceDto.builder()
                .id(price.getId())
                .plan(price.getPlan())
                .billingCycle(price.getBillingCycle())
                .amount(price.getAmount())
                .currency(price.getCurrency())
                .active(price.isActive())
                .build();
    }
}

