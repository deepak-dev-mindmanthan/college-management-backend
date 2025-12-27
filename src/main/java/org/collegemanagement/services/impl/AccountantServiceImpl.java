package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.accountant.AccountantResponse;
import org.collegemanagement.dto.accountant.CreateAccountantRequest;
import org.collegemanagement.dto.accountant.UpdateAccountantRequest;
import org.collegemanagement.entity.staff.StaffProfile;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.Role;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.enums.Status;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.AccountantMapper;
import org.collegemanagement.repositories.AccountantRepository;
import org.collegemanagement.repositories.StaffProfileRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.AccountantService;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.RoleService;
import org.collegemanagement.services.UserManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountantServiceImpl implements AccountantService {

    private final AccountantRepository accountantRepository;
    private final UserManager userManager;
    private final RoleService roleService;
    private final StaffProfileRepository staffProfileRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public AccountantResponse createAccountant(CreateAccountantRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate email uniqueness within college
        if (accountantRepository.existsByEmailAndCollegeId(request.getEmail(), collegeId)) {
            throw new ResourceConflictException("Accountant with email " + request.getEmail() + " already exists in this college");
        }

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Get accountant role
        Set<Role> accountantRoles = roleService.getRoles(RoleType.ROLE_ACCOUNTANT);

        // Create user
        User accountant = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword()) // Will be hashed by UserManager
                .roles(accountantRoles)
                .college(college)
                .status(Status.ACTIVE)
                .emailVerified(false)
                .build();

        // Save user (password will be hashed)
        userManager.createUser(accountant);
        User createdUser = userManager.findByEmail(request.getEmail());

        // Create staff profile
        StaffProfile staffProfile = StaffProfile.builder()
                .college(college)
                .user(createdUser)
                .designation(request.getDesignation())
                .salary(request.getSalary())
                .joiningDate(request.getJoiningDate())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();

        staffProfileRepository.save(staffProfile);

        return AccountantMapper.toResponse(createdUser, staffProfile);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public AccountantResponse updateAccountant(String accountantUuid, UpdateAccountantRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find accountant
        User accountant = accountantRepository.findAccountantByUuidAndCollegeId(accountantUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Accountant not found with UUID: " + accountantUuid));

        // Update email if provided and validate uniqueness
        if (request.getEmail() != null && !request.getEmail().equals(accountant.getEmail())) {
            if (accountantRepository.existsByEmailAndCollegeIdAndIdNot(request.getEmail(), collegeId, accountant.getId())) {
                throw new ResourceConflictException("Accountant with email " + request.getEmail() + " already exists in this college");
            }
            accountant.setEmail(request.getEmail());
        }

        // Update name if provided
        if (request.getName() != null) {
            accountant.setName(request.getName());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            accountant.setPassword(request.getPassword()); // Will be hashed by UserManager
        }

        // Update user
        User updatedAccountant = userManager.update(accountant);

        // Update staff profile if exists
        StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(updatedAccountant.getId(), collegeId)
                .orElse(null);

        if (staffProfile != null) {
            // Update staff profile fields
            if (request.getDesignation() != null) {
                staffProfile.setDesignation(request.getDesignation());
            }
            if (request.getSalary() != null) {
                staffProfile.setSalary(request.getSalary());
            }
            if (request.getJoiningDate() != null) {
                staffProfile.setJoiningDate(request.getJoiningDate());
            }
            if (request.getPhone() != null) {
                staffProfile.setPhone(request.getPhone());
            }
            if (request.getAddress() != null) {
                staffProfile.setAddress(request.getAddress());
            }

            staffProfileRepository.save(staffProfile);
        }

        return AccountantMapper.toResponse(updatedAccountant, staffProfile);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'ACCOUNTANT')")
    public AccountantResponse getAccountantByUuid(String accountantUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        User accountant = accountantRepository.findAccountantByUuidAndCollegeId(accountantUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Accountant not found with UUID: " + accountantUuid));

        StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(accountant.getId(), collegeId)
                .orElse(null);

        return AccountantMapper.toResponse(accountant, staffProfile);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<AccountantResponse> getAllAccountants(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<User> accountants = accountantRepository.findAllAccountantsByCollegeId(collegeId, pageable);

        return accountants.map(user -> {
            StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(user.getId(), collegeId)
                    .orElse(null);
            return AccountantMapper.toResponse(user, staffProfile);
        });
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<AccountantResponse> searchAccountants(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<User> accountants = accountantRepository.searchAccountantsByCollegeId(collegeId, searchTerm, pageable);

        return accountants.map(user -> {
            StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(user.getId(), collegeId)
                    .orElse(null);
            return AccountantMapper.toResponse(user, staffProfile);
        });
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void deleteAccountant(String accountantUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        User accountant = accountantRepository.findAccountantByUuidAndCollegeId(accountantUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Accountant not found with UUID: " + accountantUuid));

        // Check if accountant has any active fee payments (can be enhanced with additional checks)
        // For now, we'll allow deletion - the system can track historical data

        // Delete staff profile if exists
        staffProfileRepository.findByUserIdAndCollegeId(accountant.getId(), collegeId)
                .ifPresent(staffProfileRepository::delete);

        // Delete user (this will cascade appropriately based on entity relationships)
        userManager.deleteUserById(accountant.getId());
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }
}

