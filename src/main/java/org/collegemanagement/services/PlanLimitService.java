package org.collegemanagement.services;

import org.collegemanagement.entity.subscription.SubscriptionPlan;

/**
 * Service for checking and enforcing subscription plan limits.
 */
public interface PlanLimitService {

    /**
     * Check if college can add more students
     */
    boolean canAddStudent(Long collegeId);

    /**
     * Check if college can add more teachers
     */
    boolean canAddTeacher(Long collegeId);

    /**
     * Check if college can add more departments
     */
    boolean canAddDepartment(Long collegeId);

    /**
     * Get current plan limits for college
     */
    SubscriptionPlan getCurrentPlanLimits(Long collegeId);

    /**
     * Check if a specific limit is exceeded
     */
    boolean isLimitExceeded(Long collegeId, String limitType, int currentCount);
}

