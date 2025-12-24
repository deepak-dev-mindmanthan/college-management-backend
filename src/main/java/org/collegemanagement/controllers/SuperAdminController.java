package org.collegemanagement.controllers;

import jakarta.validation.Valid;
import org.collegemanagement.dto.*;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/superuser")
@Transactional
public class SuperAdminController {

    private final CollegeService collegeService;
    private final UserManager userManager;
    private final RoleService roleService;
    private final SubscriptionService subscriptionService;
    private final CollegeRegistrationService collegeRegistrationService;

    public SuperAdminController(CollegeService collegeService, UserManager userManager, RoleService roleService, SubscriptionService subscriptionService, CollegeRegistrationService collegeRegistrationService) {
        this.collegeService = collegeService;
        this.userManager = userManager;
        this.roleService = roleService;
        this.subscriptionService = subscriptionService;
        this.collegeRegistrationService = collegeRegistrationService;
    }

    @PostMapping("/college/create")
    public ResponseEntity<?> createCollege(@Valid @RequestBody RegisterCollegeRequest request) {
        if (collegeService.existsByName(request.getCollegeName())) {
            return ResponseEntity.badRequest().body("College with this name already exists.");
        }

        if (userManager.userExists(request.getAdminEmail())) {
            return ResponseEntity.badRequest().body("Admin Email already used.");
        }

        return ResponseEntity.ok(collegeRegistrationService.registerCollegeTenant(request));
    }

    @GetMapping("/college/all")
    public ResponseEntity<List<CollegeDto>> getAllColleges() {
        return ResponseEntity.ok(collegeService.findAll());
    }

    @PutMapping("/college/update/{id}")
    public ResponseEntity<?> updateCollege(@RequestBody CollegeRequest request) {

        if (!collegeService.existsById(request.getId())) {
            return ResponseEntity.badRequest().body(String.format("No college found with id:%s to update", request.getId()));
        }

        if (userManager.userExists(request.getAdminEmail())) {
            return ResponseEntity.badRequest().body("Admin Email already used.");
        }


        CollegeDto collegeDto = CollegeDto.builder()
                .id(request.getId())
                .name(request.getCollegeName())
                .email(request.getCollegeEmail())
                .phone(request.getCollegePhone())
                .address(request.getCollegeAddress())
                .status(request.getStatus())
                .build();

//        CollegeDto college = collegeService.create();
        College collegeEntity = collegeService.findByEmail(collegeDto.getEmail());
        if (request.getSubscriptionPlan() != null || request.getBillingCycle() != null) {
            SubscriptionDto subscription = SubscriptionDto.fromEntity(subscriptionService.createSubscriptionForCollege(
                    collegeEntity,
                    SubscriptionRequest.builder()
                            .plan(request.getSubscriptionPlan())
                            .billingCycle(request.getBillingCycle())
                            .build()
            ));
//            college.setSubscription(subscription);
        }
//        return ResponseEntity.ok(college);
        return null;
    }


    @DeleteMapping("/college/delete/{id}")
    public ResponseEntity<?> deleteCollege(@PathVariable Long id) {
        collegeService.deleteCollege(id);
        return ResponseEntity.ok("College deleted successfully.");
    }

    @PostMapping("/subscription/price")
    public ResponseEntity<?> upsertPlanPrice(@RequestBody PlanPriceRequest request) {
        if (request.getPlan() == null || request.getBillingCycle() == null || request.getAmount() == null) {
            return ResponseEntity.badRequest().body("plan, billingCycle and amount are required.");
        }
        //TODO:To be implemented
        return null;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Long>> getSuperAdminDashboard() {
        Map<String, Long> dashboardData = new HashMap<>();
        dashboardData.put("totalColleges", collegeService.count());
        dashboardData.put("totalStudents", userManager.countByRole(roleService.getRoleByName(RoleType.ROLE_STUDENT)));
        dashboardData.put("totalTeachers", userManager.countByRole(roleService.getRoleByName(RoleType.ROLE_TEACHER)));
        dashboardData.put("totalParents", userManager.countByRole(roleService.getRoleByName(RoleType.ROLE_PARENT)));
        dashboardData.put("totalSuperAdmins", userManager.countByRole(roleService.getRoleByName(RoleType.ROLE_SUPER_ADMIN)));
        return ResponseEntity.ok(dashboardData);
    }

}
