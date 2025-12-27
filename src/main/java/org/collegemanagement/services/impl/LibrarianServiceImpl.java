package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.librarian.CreateLibrarianRequest;
import org.collegemanagement.dto.librarian.LibrarianResponse;
import org.collegemanagement.dto.librarian.UpdateLibrarianRequest;
import org.collegemanagement.entity.staff.StaffProfile;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.Role;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.enums.Status;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.LibrarianMapper;
import org.collegemanagement.repositories.LibrarianRepository;
import org.collegemanagement.repositories.StaffProfileRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.LibrarianService;
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
public class LibrarianServiceImpl implements LibrarianService {

    private final LibrarianRepository librarianRepository;
    private final UserManager userManager;
    private final RoleService roleService;
    private final StaffProfileRepository staffProfileRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public LibrarianResponse createLibrarian(CreateLibrarianRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate email uniqueness within college
        if (librarianRepository.existsByEmailAndCollegeId(request.getEmail(), collegeId)) {
            throw new ResourceConflictException("Librarian with email " + request.getEmail() + " already exists in this college");
        }

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Get librarian role
        Set<Role> librarianRoles = roleService.getRoles(RoleType.ROLE_LIBRARIAN);

        // Create user
        User librarian = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword()) // Will be hashed by UserManager
                .roles(librarianRoles)
                .college(college)
                .status(Status.ACTIVE)
                .emailVerified(false)
                .build();

        // Save user (password will be hashed)
        userManager.createUser(librarian);
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

        return LibrarianMapper.toResponse(createdUser, staffProfile);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public LibrarianResponse updateLibrarian(String librarianUuid, UpdateLibrarianRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find librarian
        User librarian = librarianRepository.findLibrarianByUuidAndCollegeId(librarianUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Librarian not found with UUID: " + librarianUuid));

        // Update email if provided and validate uniqueness
        if (request.getEmail() != null && !request.getEmail().equals(librarian.getEmail())) {
            if (librarianRepository.existsByEmailAndCollegeIdAndIdNot(request.getEmail(), collegeId, librarian.getId())) {
                throw new ResourceConflictException("Librarian with email " + request.getEmail() + " already exists in this college");
            }
            librarian.setEmail(request.getEmail());
        }

        // Update name if provided
        if (request.getName() != null) {
            librarian.setName(request.getName());
        }

        // Update password if provided
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            librarian.setPassword(request.getPassword()); // Will be hashed by UserManager
        }

        // Update user
        User updatedLibrarian = userManager.update(librarian);

        // Update staff profile if exists
        StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(updatedLibrarian.getId(), collegeId)
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

        return LibrarianMapper.toResponse(updatedLibrarian, staffProfile);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'LIBRARIAN')")
    public LibrarianResponse getLibrarianByUuid(String librarianUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        User librarian = librarianRepository.findLibrarianByUuidAndCollegeId(librarianUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Librarian not found with UUID: " + librarianUuid));

        StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(librarian.getId(), collegeId)
                .orElse(null);

        return LibrarianMapper.toResponse(librarian, staffProfile);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<LibrarianResponse> getAllLibrarians(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<User> librarians = librarianRepository.findAllLibrariansByCollegeId(collegeId, pageable);

        return librarians.map(user -> {
            StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(user.getId(), collegeId)
                    .orElse(null);
            return LibrarianMapper.toResponse(user, staffProfile);
        });
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public Page<LibrarianResponse> searchLibrarians(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<User> librarians = librarianRepository.searchLibrariansByCollegeId(collegeId, searchTerm, pageable);

        return librarians.map(user -> {
            StaffProfile staffProfile = staffProfileRepository.findByUserIdAndCollegeId(user.getId(), collegeId)
                    .orElse(null);
            return LibrarianMapper.toResponse(user, staffProfile);
        });
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public void deleteLibrarian(String librarianUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        User librarian = librarianRepository.findLibrarianByUuidAndCollegeId(librarianUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Librarian not found with UUID: " + librarianUuid));

        // Check if librarian has any active library issues (issued by them)
        // This could be checked via LibraryIssue.issuedBy relationship
        // For now, we'll allow deletion - the system can track historical data

        // Delete staff profile if exists
        staffProfileRepository.findByUserIdAndCollegeId(librarian.getId(), collegeId)
                .ifPresent(staffProfileRepository::delete);

        // Delete user (this will cascade appropriately based on entity relationships)
        userManager.deleteUserById(librarian.getId());
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }
}

