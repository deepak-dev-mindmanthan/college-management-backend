package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.hostel.CreateHostelWardenRequest;
import org.collegemanagement.dto.hostel.HostelResponse;
import org.collegemanagement.dto.hostel.HostelWardenResponse;
import org.collegemanagement.dto.hostel.UpdateHostelWardenRequest;
import org.collegemanagement.entity.hostel.Hostel;
import org.collegemanagement.entity.staff.StaffProfile;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.Role;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.AuditAction;
import org.collegemanagement.enums.AuditEntityType;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.enums.Status;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.HostelMapper;
import org.collegemanagement.mapper.HostelWardenMapper;
import org.collegemanagement.repositories.HostelRepository;
import org.collegemanagement.repositories.HostelWardenRepository;
import org.collegemanagement.repositories.StaffProfileRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.AuditService;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.HostelWardenService;
import org.collegemanagement.services.RoleService;
import org.collegemanagement.services.UserManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HostelWardenServiceImpl implements HostelWardenService {

    private final HostelWardenRepository hostelWardenRepository;
    private final HostelRepository hostelRepository;
    private final UserManager userManager;
    private final RoleService roleService;
    private final StaffProfileRepository staffProfileRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;
    private final AuditService auditService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelWardenResponse createHostelWarden(CreateHostelWardenRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate email uniqueness within college
        if (hostelWardenRepository.existsByEmailAndCollegeId(request.getEmail(), collegeId)) {
            throw new ResourceConflictException("Hostel warden with email " + request.getEmail() + " already exists in this college");
        }

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Get hostel warden role
        Set<Role> wardenRoles = roleService.getRoles(RoleType.ROLE_HOSTEL_WARDEN);

        // Create user
        User warden = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword()) // Will be hashed by UserManager
                .roles(wardenRoles)
                .college(college)
                .status(Status.ACTIVE)
                .emailVerified(false)
                .build();

        // Save user (password will be hashed)
        userManager.createUser(warden);
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

        // Get assigned hostels
        List<String> assignedHostelUuids = hostelWardenRepository.findAssignedHostelUuidsByWardenIdAndCollegeId(createdUser.getId(), collegeId);

        // Create audit log
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            auditService.createAuditLog(
                    currentUser.getId(),
                    AuditAction.CREATE,
                    AuditEntityType.HOSTEL_WARDEN,
                    createdUser.getId(),
                    "Created hostel warden: " + createdUser.getName()
            );
        }

        return HostelWardenMapper.toResponse(createdUser, staffProfile, assignedHostelUuids);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelWardenResponse updateHostelWarden(String wardenUuid, UpdateHostelWardenRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find warden
        User warden = hostelWardenRepository.findHostelWardenByUuidAndCollegeId(wardenUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel warden not found with UUID: " + wardenUuid));

        // Update email if provided and validate uniqueness
        if (request.getEmail() != null && !request.getEmail().equals(warden.getEmail())) {
            if (hostelWardenRepository.existsByEmailAndCollegeIdAndIdNot(request.getEmail(), collegeId, warden.getId())) {
                throw new ResourceConflictException("Hostel warden with email " + request.getEmail() + " already exists in this college");
            }
            warden.setEmail(request.getEmail());
        }

        // Update name if provided
        if (request.getName() != null) {
            warden.setName(request.getName());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            warden.setPassword(request.getPassword()); // Will be hashed by UserManager
        }

        // Update user
        User updatedWarden = userManager.update(warden);

        // Update staff profile if exists
        StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(updatedWarden.getId(), collegeId)
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

        // Get assigned hostels
        List<String> assignedHostelUuids = hostelWardenRepository.findAssignedHostelUuidsByWardenIdAndCollegeId(updatedWarden.getId(), collegeId);

        // Create audit log
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            auditService.createAuditLog(
                    currentUser.getId(),
                    AuditAction.UPDATE,
                    AuditEntityType.HOSTEL_WARDEN,
                    updatedWarden.getId(),
                    "Updated hostel warden: " + updatedWarden.getName()
            );
        }

        return HostelWardenMapper.toResponse(updatedWarden, staffProfile, assignedHostelUuids);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'HOSTEL_WARDEN')")
    public HostelWardenResponse getHostelWardenByUuid(String wardenUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        User warden = hostelWardenRepository.findHostelWardenByUuidAndCollegeId(wardenUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel warden not found with UUID: " + wardenUuid));

        StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(warden.getId(), collegeId)
                .orElse(null);

        // Get assigned hostels
        List<String> assignedHostelUuids = hostelWardenRepository.findAssignedHostelUuidsByWardenIdAndCollegeId(warden.getId(), collegeId);

        return HostelWardenMapper.toResponse(warden, staffProfile, assignedHostelUuids);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public Page<HostelWardenResponse> getAllHostelWardens(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<User> wardens = hostelWardenRepository.findAllHostelWardensByCollegeId(collegeId, pageable);

        return wardens.map(user -> {
            StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(user.getId(), collegeId)
                    .orElse(null);
            List<String> assignedHostelUuids = hostelWardenRepository.findAssignedHostelUuidsByWardenIdAndCollegeId(user.getId(), collegeId);
            return HostelWardenMapper.toResponse(user, staffProfile, assignedHostelUuids);
        });
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public Page<HostelWardenResponse> searchHostelWardens(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<User> wardens = hostelWardenRepository.searchHostelWardensByCollegeId(collegeId, searchTerm, pageable);

        return wardens.map(user -> {
            StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(user.getId(), collegeId)
                    .orElse(null);
            List<String> assignedHostelUuids = hostelWardenRepository.findAssignedHostelUuidsByWardenIdAndCollegeId(user.getId(), collegeId);
            return HostelWardenMapper.toResponse(user, staffProfile, assignedHostelUuids);
        });
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'HOSTEL_WARDEN')")
    public List<HostelResponse> getHostelsByWarden(String wardenUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate warden exists
        User warden = hostelWardenRepository.findHostelWardenByUuidAndCollegeId(wardenUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel warden not found with UUID: " + wardenUuid));

        // Find all hostels assigned to this warden
        List<Hostel> hostels = hostelRepository.findAllByCollegeId(collegeId, Pageable.unpaged())
                .getContent()
                .stream()
                .filter(hostel -> hostel.getWarden() != null && hostel.getWarden().getId().equals(warden.getId()))
                .collect(Collectors.toList());

        return hostels.stream()
                .map(HostelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public void deleteHostelWarden(String wardenUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        User warden = hostelWardenRepository.findHostelWardenByUuidAndCollegeId(wardenUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel warden not found with UUID: " + wardenUuid));

        // Check if warden is assigned to any hostels
        List<String> assignedHostelUuids = hostelWardenRepository.findAssignedHostelUuidsByWardenIdAndCollegeId(warden.getId(), collegeId);
        if (!assignedHostelUuids.isEmpty()) {
            throw new ResourceConflictException("Cannot delete warden assigned to " + assignedHostelUuids.size() + " hostel(s). Please unassign from all hostels first.");
        }

        // Delete staff profile if exists
        staffProfileRepository.findByUserIdAndCollegeId(warden.getId(), collegeId)
                .ifPresent(staffProfileRepository::delete);

        Long wardenId = warden.getId();
        String wardenName = warden.getName();

        // Delete user (this will cascade appropriately based on entity relationships)
        userManager.deleteUserById(warden.getId());

        // Create audit log
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            auditService.createAuditLog(
                    currentUser.getId(),
                    AuditAction.DELETE,
                    AuditEntityType.HOSTEL_WARDEN,
                    wardenId,
                    "Deleted hostel warden: " + wardenName
            );
        }
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }

    private User getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            }
        } catch (Exception e) {
            log.debug("Could not get current user: {}", e.getMessage());
        }
        return null;
    }
}

