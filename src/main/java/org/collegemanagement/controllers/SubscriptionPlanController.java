package org.collegemanagement.controllers;

import lombok.RequiredArgsConstructor;
import org.collegemanagement.entity.subscription.SubscriptionPlan;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlanType;
import org.collegemanagement.services.SubscriptionPlanService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@RequiredArgsConstructor
public class SubscriptionPlanController {

    private final SubscriptionPlanService planService;

    /**
     * Public – used on pricing page
     */
    @GetMapping
    public ResponseEntity<List<SubscriptionPlan>> getActivePlans() {
        return ResponseEntity.ok(planService.getActivePlans());
    }

    /**
     * Public – used during checkout
     */
    @GetMapping("/{planType}/{billingCycle}")
    public ResponseEntity<SubscriptionPlan> getPlan(
            @PathVariable SubscriptionPlanType planType,
            @PathVariable BillingCycle billingCycle
    ) {
        return ResponseEntity.ok(
                planService.getActivePlan(planType, billingCycle)
        );
    }

    /**
     * SUPER_ADMIN only
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<SubscriptionPlan> createPlan(
            @RequestBody SubscriptionPlan plan
    ) {
        return ResponseEntity.ok(planService.createPlan(plan));
    }

    /**
     * SUPER_ADMIN only
     */
    @DeleteMapping("/{planId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deactivatePlan(
            @PathVariable Long planId
    ) {
        planService.deactivatePlan(planId);
        return ResponseEntity.noContent().build();
    }
}
