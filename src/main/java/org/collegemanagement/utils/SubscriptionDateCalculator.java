package org.collegemanagement.utils;

import org.collegemanagement.enums.BillingCycle;

import java.time.LocalDate;

public final class SubscriptionDateCalculator {

    private SubscriptionDateCalculator() {}

    public static LocalDate calculateExpiry(LocalDate startDate, BillingCycle cycle) {

        return switch (cycle) {
            case MONTHLY -> startDate.plusMonths(1).minusDays(1);
            case QUARTERLY -> startDate.plusMonths(3).minusDays(1);
            case YEARLY -> startDate.plusYears(1).minusDays(1);
        };
    }
}

