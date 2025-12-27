package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.transport.CreateTransportManagerRequest;
import org.collegemanagement.dto.transport.TransportManagerResponse;
import org.collegemanagement.dto.transport.UpdateTransportManagerRequest;
import org.collegemanagement.entity.staff.StaffProfile;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.Role;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.enums.Status;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.TransportManagerMapper;
import org.collegemanagement.repositories.StaffProfileRepository;
import org.collegemanagement.repositories.TransportManagerRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.RoleService;
import org.collegemanagement.services.TransportManagerService;
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
public class TransportManagerServiceImpl implements TransportManagerService {

    private final TransportManagerRepository transportManagerRepository;
    private final UserManager userManager;
    private final RoleService roleService;
    private final StaffProfileRepository staffProfileRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public TransportManagerResponse createTransportManager(CreateTransportManagerRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate email uniqueness within college
        if (transportManagerRepository.existsByEmailAndCollegeId(request.getEmail(), collegeId)) {
            throw new ResourceConflictException("Transport manager with email " + request.getEmail() + " already exists in this college");
        }

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Get transport manager role
        Set<Role> transportManagerRoles = roleService.getRoles(RoleType.ROLE_TRANSPORT_MANAGER);

        // Create user
        User transportManager = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword()) // Will be hashed by UserManager
                .roles(transportManagerRoles)
                .college(college)
                .status(Status.ACTIVE)
                .emailVerified(false)
                .build();

        // Save user (password will be hashed)
        userManager.createUser(transportManager);
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

        return TransportManagerMapper.toResponse(createdUser, staffProfile);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public TransportManagerResponse updateTransportManager(String transportManagerUuid, UpdateTransportManagerRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find transport manager
        User transportManager = transportManagerRepository.findTransportManagerByUuidAndCollegeId(transportManagerUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport manager not found with UUID: " + transportManagerUuid));

        // Update email if provided and validate uniqueness
        if (request.getEmail() != null && !request.getEmail().equals(transportManager.getEmail())) {
            if (transportManagerRepository.existsByEmailAndCollegeIdAndIdNot(request.getEmail(), collegeId, transportManager.getId())) {
                throw new ResourceConflictException("Transport manager with email " + request.getEmail() + " already exists in this college");
            }
            transportManager.setEmail(request.getEmail());
        }

        // Update name if provided
        if (request.getName() != null) {
            transportManager.setName(request.getName());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            transportManager.setPassword(request.getPassword()); // Will be hashed by UserManager
        }

        // Update user
        User updatedTransportManager = userManager.update(transportManager);

        // Update staff profile if exists
        StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(updatedTransportManager.getId(), collegeId)
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

        return TransportManagerMapper.toResponse(updatedTransportManager, staffProfile);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public TransportManagerResponse getTransportManagerByUuid(String transportManagerUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        User transportManager = transportManagerRepository.findTransportManagerByUuidAndCollegeId(transportManagerUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport manager not found with UUID: " + transportManagerUuid));

        StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(transportManager.getId(), collegeId)
                .orElse(null);

        return TransportManagerMapper.toResponse(transportManager, staffProfile);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<TransportManagerResponse> getAllTransportManagers(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<User> transportManagers = transportManagerRepository.findAllTransportManagersByCollegeId(collegeId, pageable);

        return transportManagers.map(user -> {
            StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(user.getId(), collegeId)
                    .orElse(null);
            return TransportManagerMapper.toResponse(user, staffProfile);
        });
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<TransportManagerResponse> searchTransportManagers(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<User> transportManagers = transportManagerRepository.searchTransportManagersByCollegeId(collegeId, searchTerm, pageable);

        return transportManagers.map(user -> {
            StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(user.getId(), collegeId)
                    .orElse(null);
            return TransportManagerMapper.toResponse(user, staffProfile);
        });
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void deleteTransportManager(String transportManagerUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        User transportManager = transportManagerRepository.findTransportManagerByUuidAndCollegeId(transportManagerUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport manager not found with UUID: " + transportManagerUuid));

        // Delete staff profile if exists
        staffProfileRepository.findByUserIdAndCollegeId(transportManager.getId(), collegeId)
                .ifPresent(staffProfileRepository::delete);

        // Delete user (this will cascade appropriately based on entity relationships)
        userManager.deleteUserById(transportManager.getId());
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }
}

