package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.hostel.CreateHostelRequest;
import org.collegemanagement.dto.hostel.HostelResponse;
import org.collegemanagement.dto.hostel.UpdateHostelRequest;
import org.collegemanagement.entity.hostel.Hostel;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.HostelMapper;
import org.collegemanagement.repositories.HostelRepository;
import org.collegemanagement.repositories.HostelWardenRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.HostelService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HostelServiceImpl implements HostelService {

    private final HostelRepository hostelRepository;
    private final HostelWardenRepository hostelWardenRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelResponse createHostel(CreateHostelRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Check if hostel name already exists for this college
        if (hostelRepository.existsByNameAndCollegeId(request.getName(), collegeId)) {
            throw new ResourceConflictException("Hostel with name '" + request.getName() + "' already exists in this college");
        }

        // Create hostel
        Hostel hostel = Hostel.builder()
                .college(college)
                .name(request.getName())
                .type(request.getType())
                .capacity(request.getCapacity())
                .warden(null) // Will be set if wardenUuid is provided
                .build();

        // Assign warden if provided
        if (request.getWardenUuid() != null && !request.getWardenUuid().isBlank()) {
            User warden = hostelWardenRepository.findHostelWardenByUuidAndCollegeId(request.getWardenUuid(), collegeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hostel warden not found with UUID: " + request.getWardenUuid()));
            hostel.setWarden(warden);
        }

        hostel = hostelRepository.save(hostel);

        return HostelMapper.toResponse(hostel);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelResponse updateHostel(String hostelUuid, UpdateHostelRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find hostel
        Hostel hostel = hostelRepository.findByUuidAndCollegeId(hostelUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with UUID: " + hostelUuid));

        // Update name if provided and validate uniqueness
        if (request.getName() != null && !request.getName().equals(hostel.getName())) {
            if (hostelRepository.existsByNameAndCollegeIdAndIdNot(request.getName(), collegeId, hostel.getId())) {
                throw new ResourceConflictException("Hostel with name '" + request.getName() + "' already exists in this college");
            }
            hostel.setName(request.getName());
        }

        // Update type if provided
        if (request.getType() != null) {
            hostel.setType(request.getType());
        }

        // Update capacity if provided
        if (request.getCapacity() != null) {
            hostel.setCapacity(request.getCapacity());
        }

        // Update warden if provided
        if (request.getWardenUuid() != null) {
            if (request.getWardenUuid().isBlank()) {
                // Remove warden
                hostel.setWarden(null);
            } else {
                // Assign new warden - validate it's actually a hostel warden
                User warden = hostelWardenRepository.findHostelWardenByUuidAndCollegeId(request.getWardenUuid(), collegeId)
                        .orElseThrow(() -> new ResourceNotFoundException("Hostel warden not found with UUID: " + request.getWardenUuid()));
                hostel.setWarden(warden);
            }
        }

        hostel = hostelRepository.save(hostel);

        return HostelMapper.toResponse(hostel);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public HostelResponse getHostelByUuid(String hostelUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Hostel hostel = hostelRepository.findByUuidAndCollegeId(hostelUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with UUID: " + hostelUuid));

        return HostelMapper.toResponse(hostel);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public Page<HostelResponse> getAllHostels(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<Hostel> hostels = hostelRepository.findAllByCollegeId(collegeId, pageable);

        return hostels.map(HostelMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public List<HostelResponse> getAllHostels() {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        List<Hostel> hostels = hostelRepository.findAllByCollegeId(collegeId, Pageable.unpaged()).getContent();

        return hostels.stream()
                .map(HostelMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public Page<HostelResponse> getHostelsByType(org.collegemanagement.enums.HostelType type, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<Hostel> hostels = hostelRepository.findByTypeAndCollegeId(type, collegeId, pageable);

        return hostels.map(HostelMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public Page<HostelResponse> searchHostels(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<Hostel> hostels = hostelRepository.searchByCollegeId(collegeId, searchTerm, pageable);

        return hostels.map(HostelMapper::toResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public void deleteHostel(String hostelUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Hostel hostel = hostelRepository.findByUuidAndCollegeId(hostelUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with UUID: " + hostelUuid));

        // Check if hostel has rooms or allocations
        // This will be checked in HostelRoomRepository and HostelAllocationRepository
        // For now, we'll allow deletion - the system can track historical data

        hostelRepository.delete(hostel);
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }
}

