package org.collegemanagement.dto;

import lombok.Getter;
import lombok.Setter;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlan;

@Getter
@Setter
public class TenantSignUpRequest {
    // College information
    private String collegeName;
    private String collegeEmail;
    private String collegePhone;
    private String collegeAddress;

    // Admin information
    private String adminName;
    private String adminEmail;
    private String adminPassword;

    // Subscription information
    private SubscriptionPlan subscriptionPlan;
    private BillingCycle billingCycle;
}

