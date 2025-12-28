package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.entity.subscription.Subscription;
import org.collegemanagement.entity.subscription.SubscriptionPlan;
import org.collegemanagement.repositories.SubscriptionRepository;
import org.collegemanagement.services.PlanLimitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for plan limit enforcement.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class PlanLimitServiceImpl implements PlanLimitService {

    private final SubscriptionRepository subscriptionRepository;
    private final org.collegemanagement.repositories.StudentRepository studentRepository;
    private final org.collegemanagement.repositories.TeacherRepository teacherRepository;

    @Override
    public boolean canAddStudent(Long collegeId) {
        SubscriptionPlan plan = getCurrentPlanLimits(collegeId);
        if (plan == null || plan.getMaxStudents() == null) {
            return true; // No limit set
        }
        
        long currentCount = studentRepository.countByCollegeId(collegeId);
        return currentCount < plan.getMaxStudents();
    }

    @Override
    public boolean canAddTeacher(Long collegeId) {
        SubscriptionPlan plan = getCurrentPlanLimits(collegeId);
        if (plan == null || plan.getMaxTeachers() == null) {
            return true; // No limit set
        }
        
        long currentCount = teacherRepository.countByCollegeId(collegeId);
        return currentCount < plan.getMaxTeachers();
    }

    @Override
    public boolean canAddDepartment(Long collegeId) {
        SubscriptionPlan plan = getCurrentPlanLimits(collegeId);
        if (plan == null || plan.getMaxDepartments() == null) {
            return true; // No limit set
        }
        
        // TODO: Add DepartmentRepository.countByCollegeId() method
        // For now, return true if no limit enforcement needed
        // long currentCount = departmentRepository.countByCollegeId(collegeId);
        // return currentCount < plan.getMaxDepartments();
        return true; // Placeholder until DepartmentRepository has count method
    }

    @Override
    public SubscriptionPlan getCurrentPlanLimits(Long collegeId) {
        Subscription subscription = subscriptionRepository.findByCollegeId(collegeId)
                .orElse(null);
        
        if (subscription == null || !subscription.isActive()) {
            return null; // No active subscription
        }
        
        return subscription.getPlan();
    }

    @Override
    public boolean isLimitExceeded(Long collegeId, String limitType, int currentCount) {
        SubscriptionPlan plan = getCurrentPlanLimits(collegeId);
        if (plan == null) {
            return false; // No plan, no limit
        }
        
        return switch (limitType.toUpperCase()) {
            case "STUDENTS" -> plan.getMaxStudents() != null && currentCount >= plan.getMaxStudents();
            case "TEACHERS" -> plan.getMaxTeachers() != null && currentCount >= plan.getMaxTeachers();
            case "DEPARTMENTS" -> {
                // TODO: Implement when DepartmentRepository has count method
                yield false; // Placeholder
            }
            default -> false;
        };
    }
}

