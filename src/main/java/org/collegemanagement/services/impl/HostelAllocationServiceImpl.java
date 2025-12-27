package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.hostel.CreateHostelAllocationRequest;
import org.collegemanagement.dto.hostel.HostelAllocationResponse;
import org.collegemanagement.dto.hostel.HostelSummaryResponse;
import org.collegemanagement.dto.hostel.UpdateHostelAllocationRequest;
import org.collegemanagement.entity.hostel.HostelAllocation;
import org.collegemanagement.entity.hostel.HostelRoom;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.HostelAllocationMapper;
import org.collegemanagement.repositories.HostelAllocationRepository;
import org.collegemanagement.repositories.HostelRepository;
import org.collegemanagement.repositories.HostelRoomRepository;
import org.collegemanagement.repositories.StudentRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.HostelAllocationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HostelAllocationServiceImpl implements HostelAllocationService {

    private final HostelAllocationRepository hostelAllocationRepository;
    private final StudentRepository studentRepository;
    private final HostelRoomRepository hostelRoomRepository;
    private final HostelRepository hostelRepository;
    private final TenantAccessGuard tenantAccessGuard;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelAllocationResponse createHostelAllocation(CreateHostelAllocationRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find student
        Student student = studentRepository.findByUuidAndCollegeId(request.getStudentUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + request.getStudentUuid()));

        // Find room
        HostelRoom room = hostelRoomRepository.findByUuidAndCollegeId(request.getRoomUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel room not found with UUID: " + request.getRoomUuid()));

        // Check if student already has an active allocation
        if (hostelAllocationRepository.existsActiveByStudentUuidAndCollegeId(request.getStudentUuid(), collegeId)) {
            throw new ResourceConflictException("Student already has an active hostel allocation");
        }

        // Check if room has available capacity
        long currentOccupancy = hostelAllocationRepository.countActiveByRoomUuidAndCollegeId(request.getRoomUuid(), collegeId);
        if (currentOccupancy >= room.getCapacity()) {
            throw new ResourceConflictException("Room is at full capacity. Current occupancy: " + currentOccupancy + ", Capacity: " + room.getCapacity());
        }

        // Release any existing active allocations for this student (only one active allocation per student)
        List<HostelAllocation> existingActiveAllocations = hostelAllocationRepository
                .findByStudentUuidAndCollegeId(request.getStudentUuid(), collegeId)
                .stream()
                .filter(ha -> ha.getReleasedAt() == null)
                .collect(Collectors.toList());

        for (HostelAllocation existing : existingActiveAllocations) {
            existing.setReleasedAt(Instant.now());
            hostelAllocationRepository.save(existing);
        }

        // Create hostel allocation
        HostelAllocation allocation = HostelAllocation.builder()
                .student(student)
                .room(room)
                .allocatedAt(request.getAllocatedAt() != null ? request.getAllocatedAt() : Instant.now())
                .releasedAt(null)
                .build();

        allocation = hostelAllocationRepository.save(allocation);

        return HostelAllocationMapper.toResponse(allocation);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelAllocationResponse updateHostelAllocation(String allocationUuid, UpdateHostelAllocationRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find allocation
        HostelAllocation allocation = hostelAllocationRepository.findByUuidAndCollegeId(allocationUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel allocation not found with UUID: " + allocationUuid));

        // Update room if provided
        if (request.getRoomUuid() != null && !request.getRoomUuid().equals(allocation.getRoom().getUuid())) {
            HostelRoom room = hostelRoomRepository.findByUuidAndCollegeId(request.getRoomUuid(), collegeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hostel room not found with UUID: " + request.getRoomUuid()));

            // Check if new room has available capacity (only if allocation is active)
            if (allocation.getReleasedAt() == null) {
                long currentOccupancy = hostelAllocationRepository.countActiveByRoomUuidAndCollegeId(request.getRoomUuid(), collegeId);
                if (currentOccupancy >= room.getCapacity()) {
                    throw new ResourceConflictException("Room is at full capacity. Current occupancy: " + currentOccupancy + ", Capacity: " + room.getCapacity());
                }
            }

            allocation.setRoom(room);
        }

        // Update releasedAt if provided (to release/deactivate)
        if (request.getReleasedAt() != null) {
            allocation.setReleasedAt(request.getReleasedAt());
        }

        allocation = hostelAllocationRepository.save(allocation);

        return HostelAllocationMapper.toResponse(allocation);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public HostelAllocationResponse getHostelAllocationByUuid(String allocationUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        HostelAllocation allocation = hostelAllocationRepository.findByUuidAndCollegeId(allocationUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel allocation not found with UUID: " + allocationUuid));

        return HostelAllocationMapper.toResponse(allocation);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public Page<HostelAllocationResponse> getAllHostelAllocations(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<HostelAllocation> allocations = hostelAllocationRepository.findAllByCollegeId(collegeId, pageable);

        return allocations.map(HostelAllocationMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public Page<HostelAllocationResponse> getActiveHostelAllocations(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<HostelAllocation> allocations = hostelAllocationRepository.findActiveByCollegeId(collegeId, pageable);

        return allocations.map(HostelAllocationMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public List<HostelAllocationResponse> getHostelAllocationsByStudent(String studentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        List<HostelAllocation> allocations = hostelAllocationRepository.findByStudentUuidAndCollegeId(studentUuid, collegeId);

        return HostelAllocationMapper.toResponseList(allocations);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public HostelAllocationResponse getActiveHostelAllocationByStudent(String studentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        HostelAllocation allocation = hostelAllocationRepository.findActiveByStudentUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("No active hostel allocation found for student with UUID: " + studentUuid));

        return HostelAllocationMapper.toResponse(allocation);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public Page<HostelAllocationResponse> getHostelAllocationsByRoom(String roomUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate room exists
        hostelRoomRepository.findByUuidAndCollegeId(roomUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel room not found with UUID: " + roomUuid));

        Page<HostelAllocation> allocations = hostelAllocationRepository.findByRoomUuidAndCollegeId(roomUuid, collegeId, pageable);

        return allocations.map(HostelAllocationMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public List<HostelAllocationResponse> getActiveHostelAllocationsByRoom(String roomUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate room exists
        hostelRoomRepository.findByUuidAndCollegeId(roomUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel room not found with UUID: " + roomUuid));

        List<HostelAllocation> allocations = hostelAllocationRepository.findActiveByRoomUuidAndCollegeId(roomUuid, collegeId);

        return HostelAllocationMapper.toResponseList(allocations);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public Page<HostelAllocationResponse> getHostelAllocationsByHostel(String hostelUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate hostel exists
        hostelRepository.findByUuidAndCollegeId(hostelUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with UUID: " + hostelUuid));

        Page<HostelAllocation> allocations = hostelAllocationRepository.findByHostelUuidAndCollegeId(hostelUuid, collegeId, pageable);

        return allocations.map(HostelAllocationMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public List<HostelAllocationResponse> getActiveHostelAllocationsByHostel(String hostelUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate hostel exists
        hostelRepository.findByUuidAndCollegeId(hostelUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with UUID: " + hostelUuid));

        List<HostelAllocation> allocations = hostelAllocationRepository.findActiveByHostelUuidAndCollegeId(hostelUuid, collegeId);

        return HostelAllocationMapper.toResponseList(allocations);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelAllocationResponse releaseHostelAllocation(String allocationUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        HostelAllocation allocation = hostelAllocationRepository.findByUuidAndCollegeId(allocationUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel allocation not found with UUID: " + allocationUuid));

        if (allocation.getReleasedAt() != null) {
            throw new ResourceConflictException("Hostel allocation is already released");
        }

        allocation.setReleasedAt(Instant.now());
        allocation = hostelAllocationRepository.save(allocation);

        return HostelAllocationMapper.toResponse(allocation);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public Page<HostelAllocationResponse> searchHostelAllocations(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<HostelAllocation> allocations = hostelAllocationRepository.searchByCollegeId(collegeId, searchTerm, pageable);

        return allocations.map(HostelAllocationMapper::toResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public void deleteHostelAllocation(String allocationUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        HostelAllocation allocation = hostelAllocationRepository.findByUuidAndCollegeId(allocationUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel allocation not found with UUID: " + allocationUuid));

        hostelAllocationRepository.delete(allocation);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelSummaryResponse getHostelSummary() {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        long totalHostels = hostelRepository.countByCollegeId(collegeId);
        long totalRooms = hostelRoomRepository.countByCollegeId(collegeId);
        long totalActiveAllocations = hostelAllocationRepository.countActiveByCollegeId(collegeId);
        
        // Calculate total allocations (active + inactive)
        long totalAllocations = hostelAllocationRepository.findAllByCollegeId(collegeId, Pageable.unpaged()).getTotalElements();
        long totalInactiveAllocations = totalAllocations - totalActiveAllocations;
        
        long totalStudents = studentRepository.countByCollegeId(collegeId);
        
        // Count unique students with active allocations
        long totalStudentsWithHostel = hostelAllocationRepository.findActiveByCollegeId(collegeId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(ha -> ha.getStudent().getId())
                .distinct()
                .count();
        
        long totalStudentsWithoutHostel = totalStudents - totalStudentsWithHostel;

        // Calculate total capacity and occupancy
        List<org.collegemanagement.entity.hostel.HostelRoom> allRooms = hostelRoomRepository.findAllByCollegeId(collegeId, Pageable.unpaged()).getContent();
        long totalCapacity = allRooms.stream()
                .mapToLong(org.collegemanagement.entity.hostel.HostelRoom::getCapacity)
                .sum();
        
        long totalOccupied = totalActiveAllocations;
        long totalAvailable = totalCapacity - totalOccupied;

        return HostelSummaryResponse.builder()
                .totalHostels(totalHostels)
                .totalRooms(totalRooms)
                .totalActiveAllocations(totalActiveAllocations)
                .totalInactiveAllocations(totalInactiveAllocations)
                .totalStudents(totalStudents)
                .totalStudentsWithHostel(totalStudentsWithHostel)
                .totalStudentsWithoutHostel(totalStudentsWithoutHostel)
                .totalCapacity(totalCapacity)
                .totalOccupied(totalOccupied)
                .totalAvailable(totalAvailable)
                .build();
    }
}

