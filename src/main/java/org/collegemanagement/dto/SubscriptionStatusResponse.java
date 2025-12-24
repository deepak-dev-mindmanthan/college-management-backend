package org.collegemanagement.dto;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.SubscriptionPlanType;
import org.collegemanagement.enums.SubscriptionStatus;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionStatusResponse {

    /**
     * NONE, PENDING, ACTIVE, EXPIRED
     */
    private SubscriptionStatus status;

    /**
     * STARTER, STANDARD, PREMIUM
     * Null when no subscription exists
     */
    private SubscriptionPlanType planType;

    /**
     * Subscription expiry date
     * Null when not applicable
     */
    private LocalDate expiresAt;

    /**
     * Backend-calculated access decision
     */
    private boolean canAccessCoreApis;
}
