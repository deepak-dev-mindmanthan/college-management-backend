package org.collegemanagement.controllers;


import lombok.AllArgsConstructor;
import org.collegemanagement.dto.SubscriptionStatusResponse;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.security.beans.CurrentUserProvider;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/subscriptions")
@AllArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;
    private final CurrentUserProvider currentUserProvider;

    @GetMapping("/status")
    public ResponseEntity<SubscriptionStatusResponse> getStatus() {
        College college = currentUserProvider.getCurrentUser().getCollege();

        if (college == null) {
            throw new ResourceNotFoundException("No college associated with this User");
        }
        return ResponseEntity.ok(subscriptionService.getStatus(college));
    }

    @PostMapping("/subscribe")
    @PreAuthorize("hasRole('COLLEGE_ADMIN')")
    public void subscribe() {
    }

    @GetMapping("/plans")
    public List<?> plans() {
        return null;//SubscriptionPlanResponse
    }

    @PostMapping("/upgrade")
    @PreAuthorize("hasRole('COLLEGE_ADMIN')")
    public void upgrade() {
    }
}
