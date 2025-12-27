package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.hostel.CreateHostelManagerRequest;
import org.collegemanagement.dto.hostel.HostelManagerResponse;
import org.collegemanagement.dto.hostel.UpdateHostelManagerRequest;
import org.collegemanagement.entity.staff.StaffProfile;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.Role;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.enums.Status;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.HostelManagerMapper;
import org.collegemanagement.repositories.HostelManagerRepository;
import org.collegemanagement.repositories.StaffProfileRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.HostelManagerService;
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
public class HostelManagerServiceImpl implements HostelManagerService {

    private final HostelManagerRepository hostelManagerRepository;
    private final UserManager userManager;
    private final RoleService roleService;
    private final StaffProfileRepository staffProfileRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public HostelManagerResponse createHostelManager(CreateHostelManagerRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate email uniqueness within college
        if (hostelManagerRepository.existsByEmailAndCollegeId(request.getEmail(), collegeId)) {
            throw new ResourceConflictException("Hostel manager with email " + request.getEmail() + " already exists in this college");
        }

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Get hostel manager role
        Set<Role> hostelManagerRoles = roleService.getRoles(RoleType.ROLE_HOSTEL_MANAGER);

        // Create user
        User hostelManager = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword()) // Will be hashed by UserManager
                .roles(hostelManagerRoles)
                .college(college)
                .status(Status.ACTIVE)
                .emailVerified(false)
                .build();

        // Save user (password will be hashed)
        userManager.createUser(hostelManager);
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

        return HostelManagerMapper.toResponse(createdUser, staffProfile);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public HostelManagerResponse updateHostelManager(String hostelManagerUuid, UpdateHostelManagerRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find hostel manager
        User hostelManager = hostelManagerRepository.findHostelManagerByUuidAndCollegeId(hostelManagerUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel manager not found with UUID: " + hostelManagerUuid));

        // Update email if provided and validate uniqueness
        if (request.getEmail() != null && !request.getEmail().equals(hostelManager.getEmail())) {
            if (hostelManagerRepository.existsByEmailAndCollegeIdAndIdNot(request.getEmail(), collegeId, hostelManager.getId())) {
                throw new ResourceConflictException("Hostel manager with email " + request.getEmail() + " already exists in this college");
            }
            hostelManager.setEmail(request.getEmail());
        }

        // Update name if provided
        if (request.getName() != null) {
            hostelManager.setName(request.getName());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            hostelManager.setPassword(request.getPassword()); // Will be hashed by UserManager
        }

        // Update user
        User updatedHostelManager = userManager.update(hostelManager);

        // Update staff profile if exists
        StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(updatedHostelManager.getId(), collegeId)
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

        return HostelManagerMapper.toResponse(updatedHostelManager, staffProfile);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelManagerResponse getHostelManagerByUuid(String hostelManagerUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        User hostelManager = hostelManagerRepository.findHostelManagerByUuidAndCollegeId(hostelManagerUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel manager not found with UUID: " + hostelManagerUuid));

        StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(hostelManager.getId(), collegeId)
                .orElse(null);

        return HostelManagerMapper.toResponse(hostelManager, staffProfile);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<HostelManagerResponse> getAllHostelManagers(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<User> hostelManagers = hostelManagerRepository.findAllHostelManagersByCollegeId(collegeId, pageable);

        return hostelManagers.map(user -> {
            StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(user.getId(), collegeId)
                    .orElse(null);
            return HostelManagerMapper.toResponse(user, staffProfile);
        });
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<HostelManagerResponse> searchHostelManagers(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<User> hostelManagers = hostelManagerRepository.searchHostelManagersByCollegeId(collegeId, searchTerm, pageable);

        return hostelManagers.map(user -> {
            StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(user.getId(), collegeId)
                    .orElse(null);
            return HostelManagerMapper.toResponse(user, staffProfile);
        });
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void deleteHostelManager(String hostelManagerUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        User hostelManager = hostelManagerRepository.findHostelManagerByUuidAndCollegeId(hostelManagerUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel manager not found with UUID: " + hostelManagerUuid));

        // Delete staff profile if exists
        staffProfileRepository.findByUserIdAndCollegeId(hostelManager.getId(), collegeId)
                .ifPresent(staffProfileRepository::delete);

        // Delete user (this will cascade appropriately based on entity relationships)
        userManager.deleteUserById(hostelManager.getId());
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }
}

