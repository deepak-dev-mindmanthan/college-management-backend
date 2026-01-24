package org.collegemanagement.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.dto.plan.CreateSubscriptionPlanRequest;
import org.collegemanagement.dto.plan.SubscriptionPlanResponse;
import org.collegemanagement.dto.plan.UpdateSubscriptionPlanRequest;
import org.collegemanagement.entity.subscription.SubscriptionPlan;
import org.collegemanagement.enums.BillingCycle;
import org.collegemanagement.enums.SubscriptionPlanType;
import org.collegemanagement.repositories.SubscriptionPlanRepository;
import org.collegemanagement.services.SubscriptionPlanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/subscription-plans")
@AllArgsConstructor
@Tag(name = "Subscription Plan Management", description = "APIs for managing subscription plans")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Operation(
            summary = "Get all active plans",
            description = "Retrieves all active subscription plans. Public endpoint."
    )
    @GetMapping
    public ResponseEntity<ApiResponse<List<SubscriptionPlanResponse>>> getActivePlans() {
        List<SubscriptionPlan> plans = subscriptionPlanService.getActivePlans();
        List<SubscriptionPlanResponse> responses = plans.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(responses, "Active plans retrieved successfully", HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get plan by type and billing cycle",
            description = "Retrieves a specific active plan by type and billing cycle. Public endpoint."
    )
    @GetMapping("/{planType}/{billingCycle}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlan(
            @Parameter(description = "Plan type")
            @PathVariable SubscriptionPlanType planType,
            @Parameter(description = "Billing cycle")
            @PathVariable BillingCycle billingCycle
    ) {
        SubscriptionPlan plan = subscriptionPlanService.getActivePlan(planType, billingCycle);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(plan), "Plan retrieved successfully", HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Create a new subscription plan",
            description = "Creates a new subscription plan. Requires SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Plan created successfully",
                    content = @Content(schema = @Schema(implementation = SubscriptionPlanResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Plan already exists for given code and billing cycle"
            )
    })
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> createPlan(
            @Valid @RequestBody CreateSubscriptionPlanRequest request
    ) {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .code(request.getPlanType())
                .billingCycle(request.getBillingCycle())
                .price(request.getPrice())
                .currency(request.getCurrency())
                .active(true)
                .build();

        plan = subscriptionPlanService.createPlan(plan);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(mapToResponse(plan), "Plan created successfully", HttpStatus.CREATED.value()));
    }

    @Operation(
            summary = "Update subscription plan",
            description = "Updates subscription plan details. Requires SUPER_ADMIN role."
    )
    @PutMapping("/{planId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> updatePlan(
            @Parameter(description = "ID of the plan to update")
            @PathVariable Long planId,
            @Valid @RequestBody UpdateSubscriptionPlanRequest request
    ) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new org.collegemanagement.exception.ResourceNotFoundException("Plan not found"));

        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }
        if (request.getCurrency() != null) {
            plan.setCurrency(request.getCurrency());
        }
        if (request.getActive() != null) {
            plan.setActive(request.getActive());
        }

        plan = subscriptionPlanRepository.save(plan);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(plan), "Plan updated successfully", HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Deactivate subscription plan",
            description = "Deactivates a subscription plan. Requires SUPER_ADMIN role."
    )
    @PutMapping("/{planId}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> deactivatePlan(
            @Parameter(description = "ID of the plan to deactivate")
            @PathVariable Long planId
    ) {
        SubscriptionPlan plan = subscriptionPlanService.deactivatePlan(planId);
        plan = subscriptionPlanRepository.save(plan);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(plan), "Plan deactivated successfully", HttpStatus.OK.value()));
    }

    @Operation(
            summary = "Get plan by ID",
            description = "Retrieves subscription plan by ID. Public endpoint."
    )
    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<SubscriptionPlanResponse>> getPlanById(
            @Parameter(description = "ID of the plan")
            @PathVariable Long planId
    ) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new org.collegemanagement.exception.ResourceNotFoundException("Plan not found"));
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(plan), "Plan retrieved successfully", HttpStatus.OK.value()));
    }

    private SubscriptionPlanResponse mapToResponse(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .planType(plan.getCode())
                .billingCycle(plan.getBillingCycle())
                .price(plan.getPrice())
                .currency(plan.getCurrency())
                .active(plan.isActive())
                .build();
    }
}

