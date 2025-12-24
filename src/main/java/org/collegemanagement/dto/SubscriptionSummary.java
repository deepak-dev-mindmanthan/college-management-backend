package org.collegemanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.collegemanagement.enums.SubscriptionPlanType;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SubscriptionSummary {
    private SubscriptionPlanType plan;
    private LocalDate expiresAt;
    private boolean canAccessCoreApis;
}
