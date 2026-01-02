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
import org.collegemanagement.dto.subscription.CreateSubscriptionRequest;
import org.collegemanagement.dto.subscription.RenewSubscriptionRequest;
import org.collegemanagement.dto.subscription.SubscriptionResponse;
import org.collegemanagement.dto.subscription.UpgradeSubscriptionRequest;
import org.collegemanagement.enums.SubscriptionStatus;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@AllArgsConstructor
@Tag(name = "Subscription Management", description = "APIs for managing college subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @Operation(
            summary = "Create a new subscription",
            description = "Creates a new subscription for the college. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Subscription created successfully",
                    content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "College already has an active subscription"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request
    ) {
        SubscriptionResponse subscription = subscriptionService.createSubscription(request);
        return ResponseEntity.ok(ApiResponse.success(subscription, "Subscription created successfully"));
    }



    @Operation(
            summary = "Upgrade subscription plan",
            description = "Upgrades an ACTIVE subscription to a higher plan. "
                    + "Subscription will move to PENDING state until payment is completed."
    )
    @PostMapping("/{subscriptionUuid}/upgrade")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> upgradeSubscription(
            @Parameter(description = "UUID of the subscription to upgrade")
            @PathVariable String subscriptionUuid,
            @Valid @RequestBody UpgradeSubscriptionRequest request
    ) {
        SubscriptionResponse subscription =
                subscriptionService.upgradeSubscription(subscriptionUuid, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        subscription,
                        "Subscription upgrade initiated successfully"
                )
        );
    }


//    @Operation(
//            summary = "Update subscription",
//            description = "Updates subscription details. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
//    )
//    @PutMapping("/{subscriptionUuid}")
//    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
//    public ResponseEntity<ApiResponse<SubscriptionResponse>> updateSubscription(
//            @Parameter(description = "UUID of the subscription to update")
//            @PathVariable String subscriptionUuid,
//            @Valid @RequestBody UpdateSubscriptionRequest request
//    ) {
//        SubscriptionResponse subscription = subscriptionService.updateSubscription(subscriptionUuid, request);
//        return ResponseEntity.ok(ApiResponse.success(subscription, "Subscription updated successfully"));
//    }

    @Operation(
            summary = "Cancel subscription",
            description = "Cancels a subscription. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/{subscriptionUuid}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(
            @Parameter(description = "UUID of the subscription to cancel")
            @PathVariable String subscriptionUuid
    ) {
        SubscriptionResponse subscription = subscriptionService.cancelSubscription(subscriptionUuid);
        return ResponseEntity.ok(ApiResponse.success(subscription, "Subscription cancelled successfully"));
    }

    @Operation(
            summary = "Renew subscription",
            description = "Renews a subscription with a new plan and billing cycle. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PostMapping("/{subscriptionUuid}/renew")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> renewSubscription(
            @Parameter(description = "UUID of the subscription to renew")
            @PathVariable String subscriptionUuid,
            @Valid @RequestBody RenewSubscriptionRequest request
    ) {
        SubscriptionResponse subscription = subscriptionService.renewSubscription(subscriptionUuid, request);
        return ResponseEntity.ok(ApiResponse.success(subscription, "Subscription renewed successfully"));
    }

    @Operation(
            summary = "Get subscription by UUID",
            description = "Retrieves subscription information by UUID. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/{subscriptionUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getSubscription(
            @Parameter(description = "UUID of the subscription")
            @PathVariable String subscriptionUuid
    ) {
        SubscriptionResponse subscription = subscriptionService.getSubscriptionByUuid(subscriptionUuid);
        return ResponseEntity.ok(ApiResponse.success(subscription, "Subscription retrieved successfully"));
    }

    @Operation(
            summary = "Get current subscription",
            description = "Retrieves the current subscription for the college. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getCurrentSubscription() {
        SubscriptionResponse subscription = subscriptionService.getCurrentSubscription();
        return ResponseEntity.ok(ApiResponse.success(subscription, "Current subscription retrieved successfully"));
    }

    @Operation(
            summary = "Get all subscriptions",
            description = "Retrieves a paginated list of all subscriptions. Requires SUPER_ADMIN role."
    )
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<SubscriptionResponse>>> getAllSubscriptions(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<SubscriptionResponse> subscriptions = subscriptionService.getAllSubscriptions(pageable);
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Subscriptions retrieved successfully"));
    }

    @Operation(
            summary = "Get subscriptions by status",
            description = "Retrieves subscriptions filtered by status. Requires SUPER_ADMIN role."
    )
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<SubscriptionResponse>>> getSubscriptionsByStatus(
            @Parameter(description = "Subscription status")
            @PathVariable SubscriptionStatus status,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Subscriptions retrieved successfully"));
    }

    @Operation(
            summary = "Get subscriptions by college",
            description = "Retrieves all subscriptions for a specific college. Requires SUPER_ADMIN role."
    )
    @GetMapping("/college/{collegeId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<SubscriptionResponse>>> getSubscriptionsByCollege(
            @Parameter(description = "ID of the college")
            @PathVariable Long collegeId,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsByCollegeId(collegeId, pageable);
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Subscriptions retrieved successfully"));
    }

    @Operation(
            summary = "Get expired subscriptions",
            description = "Retrieves all expired subscriptions. Requires SUPER_ADMIN role."
    )
    @GetMapping("/expired")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<SubscriptionResponse>>> getExpiredSubscriptions(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SubscriptionResponse> subscriptions = subscriptionService.getExpiredSubscriptions(pageable);
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Expired subscriptions retrieved successfully"));
    }

    @Operation(
            summary = "Get subscriptions expiring soon",
            description = "Retrieves subscriptions expiring within specified days. Requires SUPER_ADMIN role."
    )
    @GetMapping("/expiring-soon")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<SubscriptionResponse>>> getSubscriptionsExpiringSoon(
            @Parameter(description = "Number of days")
            @RequestParam(defaultValue = "30") int days,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SubscriptionResponse> subscriptions = subscriptionService.getSubscriptionsExpiringSoon(days, pageable);
        return ResponseEntity.ok(ApiResponse.success(subscriptions, "Subscriptions expiring soon retrieved successfully"));
    }

    @Operation(
            summary = "Activate subscription",
            description = "Activates a subscription (typically after payment). Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/{subscriptionUuid}/activate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> activateSubscription(
            @Parameter(description = "UUID of the subscription to activate")
            @PathVariable String subscriptionUuid
    ) {
        SubscriptionResponse subscription = subscriptionService.activateSubscription(subscriptionUuid);
        return ResponseEntity.ok(ApiResponse.success(subscription, "Subscription activated successfully"));
    }

    @Operation(
            summary = "Check if subscription is active",
            description = "Checks if a subscription is currently active. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/{subscriptionUuid}/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Boolean>> isSubscriptionActive(
            @Parameter(description = "UUID of the subscription")
            @PathVariable String subscriptionUuid
    ) {
        boolean isActive = subscriptionService.isSubscriptionActive(subscriptionUuid);
        return ResponseEntity.ok(ApiResponse.success(isActive, "Subscription status checked successfully"));
    }
}

