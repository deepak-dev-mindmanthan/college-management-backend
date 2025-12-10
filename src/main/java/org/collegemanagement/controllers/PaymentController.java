package org.collegemanagement.controllers;

import org.collegemanagement.dto.PaymentRequest;
import org.collegemanagement.dto.SubscriptionDto;
import org.collegemanagement.dto.SubscriptionRequest;
import org.collegemanagement.entity.College;
import org.collegemanagement.entity.User;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@PreAuthorize("hasAnyRole('SUPER_ADMIN','COLLEGE_ADMIN')")
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final CollegeService collegeService;
    private final SubscriptionService subscriptionService;

    public PaymentController(CollegeService collegeService, SubscriptionService subscriptionService) {
        this.collegeService = collegeService;
        this.subscriptionService = subscriptionService;
    }

    private Authentication currentAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isSuperAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream().anyMatch(a ->
                "ROLE_SUPER_ADMIN".equalsIgnoreCase(a.getAuthority()) || "SUPER_ADMIN".equalsIgnoreCase(a.getAuthority()));
    }

    private Long currentCollegeId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) return null;
        if (authentication.getPrincipal() instanceof User user && user.getCollege() != null) {
            return user.getCollege().getId();
        }
        return null;
    }

    private void requireSameCollege(Long targetCollegeId) {
        Authentication auth = currentAuth();
        if (isSuperAdmin(auth)) {
            return;
        }
        Long currentCollege = currentCollegeId(auth);
        if (currentCollege == null || targetCollegeId == null || !currentCollege.equals(targetCollegeId)) {
            throw new AccessDeniedException("Access denied: cross-college access is not permitted.");
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<SubscriptionDto> checkout(@RequestBody PaymentRequest request) {
        if (request.getCollegeId() == null) {
            return ResponseEntity.badRequest().build();
        }
        College college = collegeService.findById(request.getCollegeId());
        requireSameCollege(college.getId());

        SubscriptionRequest subReq = SubscriptionRequest.builder()
                .plan(request.getPlan())
                .billingCycle(request.getBillingCycle())
                .build();

        // TODO: implement payment provider integration; for now we assume payment success
        return ResponseEntity.ok(
                SubscriptionDto.fromEntity(
                        subscriptionService.createOrUpdateForCollege(college, subReq)
                )
        );
    }
}

