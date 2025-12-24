package org.collegemanagement.controllers;

import org.collegemanagement.dto.FeeRequest;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.FeeStatus;
import org.collegemanagement.services.FeeService;
import org.collegemanagement.services.UserManager;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('ACCOUNTANT','COLLEGE_ADMIN','SUPER_ADMIN')")
@RequestMapping("/api/v1/accountant")
public class AccountantController {

    private final UserManager userManager;

    public AccountantController( UserManager userManager) {
        this.userManager = userManager;
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

    private void requireSameCollege(User user) {
        if (user == null || user.getCollege() == null) {
            throw new AccessDeniedException("Access denied: user not assigned to a college.");
        }
        requireSameCollege(user.getCollege().getId());
    }

    @PostMapping("/fees")
    public ResponseEntity<?> createFee(@RequestBody FeeRequest request) {


        return ResponseEntity.ok(null);
    }

    @GetMapping("/fees/by-student")
    public ResponseEntity<List<?>> getFeesByStudent(@RequestParam Long studentId) {

        return ResponseEntity.ok(null);
    }

    @GetMapping("/fees/by-college")
    public ResponseEntity<List<?>> getFeesByCollege(@RequestParam Long collegeId, @RequestParam(required = false) FeeStatus status) {

        return ResponseEntity.ok(null);
    }

    @PutMapping("/fees/{id}/status")
    public ResponseEntity<?> updateFeeStatus(@PathVariable Long id, @RequestParam FeeStatus status) {

        return ResponseEntity.ok("");
    }

    @GetMapping("/fees/pending-total")
    public ResponseEntity<?> getPendingTotal(@RequestParam Long studentId) {
        User student = userManager.findById(studentId);
        requireSameCollege(student);
        return ResponseEntity.ok(null);
    }
}

