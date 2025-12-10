package org.collegemanagement.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.enums.SubscriptionPlan;
import org.collegemanagement.enums.SubscriptionStatus;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Token {
    private Long userId;
    private String accessToken;
    private String refreshToken;
    private SubscriptionPlan subscriptionPlan;
    private SubscriptionStatus subscriptionStatus;
    private LocalDate subscriptionExpiresAt;
}
