package org.collegemanagement.enums;

public enum SubscriptionStatus {

    NONE,        // never subscribed
    PENDING,     // payment initiated
    ACTIVE,      // valid
    TRIAL,       // Trial
    EXPIRED,     // expired, grace may apply
    CANCELLED
}

