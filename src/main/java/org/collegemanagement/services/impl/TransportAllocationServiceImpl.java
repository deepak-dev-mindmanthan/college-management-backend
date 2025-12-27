package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.transport.CreateTransportAllocationRequest;
import org.collegemanagement.dto.transport.TransportAllocationResponse;
import org.collegemanagement.dto.transport.UpdateTransportAllocationRequest;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.transport.TransportAllocation;
import org.collegemanagement.entity.transport.TransportRoute;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.TransportAllocationMapper;
import org.collegemanagement.repositories.StudentRepository;
import org.collegemanagement.repositories.TransportAllocationRepository;
import org.collegemanagement.repositories.TransportRouteRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.TransportAllocationService;
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
public class TransportAllocationServiceImpl implements TransportAllocationService {

    private final TransportAllocationRepository transportAllocationRepository;
    private final StudentRepository studentRepository;
    private final TransportRouteRepository transportRouteRepository;
    private final TenantAccessGuard tenantAccessGuard;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public TransportAllocationResponse createTransportAllocation(CreateTransportAllocationRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find student
        Student student = studentRepository.findByUuidAndCollegeId(request.getStudentUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + request.getStudentUuid()));

        // Find route
        TransportRoute route = transportRouteRepository.findByUuidAndCollegeId(request.getRouteUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport route not found with UUID: " + request.getRouteUuid()));

        // Check if student already has an active allocation for this route
        if (transportAllocationRepository.existsActiveByStudentUuidAndRouteUuidAndCollegeId(
                request.getStudentUuid(), request.getRouteUuid(), collegeId)) {
            throw new ResourceConflictException("Student already has an active transport allocation for this route");
        }

        // Release any existing active allocations for this student (only one active allocation per student)
        List<TransportAllocation> existingActiveAllocations = transportAllocationRepository
                .findByStudentUuidAndCollegeId(request.getStudentUuid(), collegeId)
                .stream()
                .filter(ta -> ta.getReleasedAt() == null)
                .collect(Collectors.toList());

        for (TransportAllocation existing : existingActiveAllocations) {
            existing.setReleasedAt(Instant.now());
            transportAllocationRepository.save(existing);
        }

        // Create transport allocation
        TransportAllocation allocation = TransportAllocation.builder()
                .student(student)
                .route(route)
                .pickupPoint(request.getPickupPoint())
                .allocatedAt(request.getAllocatedAt() != null ? request.getAllocatedAt() : Instant.now())
                .releasedAt(null)
                .build();

        allocation = transportAllocationRepository.save(allocation);

        return TransportAllocationMapper.toResponse(allocation);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public TransportAllocationResponse updateTransportAllocation(String allocationUuid, UpdateTransportAllocationRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find allocation
        TransportAllocation allocation = transportAllocationRepository.findByUuidAndCollegeId(allocationUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport allocation not found with UUID: " + allocationUuid));

        // Update route if provided
        if (request.getRouteUuid() != null && !request.getRouteUuid().equals(allocation.getRoute().getUuid())) {
            TransportRoute route = transportRouteRepository.findByUuidAndCollegeId(request.getRouteUuid(), collegeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Transport route not found with UUID: " + request.getRouteUuid()));

            // Check if student already has an active allocation for the new route
            if (allocation.getReleasedAt() == null &&
                    transportAllocationRepository.existsActiveByStudentUuidAndRouteUuidAndCollegeId(
                            allocation.getStudent().getUuid(), request.getRouteUuid(), collegeId)) {
                throw new ResourceConflictException("Student already has an active transport allocation for this route");
            }

            allocation.setRoute(route);
        }

        // Update pickup point if provided
        if (request.getPickupPoint() != null) {
            allocation.setPickupPoint(request.getPickupPoint());
        }

        // Update releasedAt if provided (to release/deactivate)
        if (request.getReleasedAt() != null) {
            allocation.setReleasedAt(request.getReleasedAt());
        }

        allocation = transportAllocationRepository.save(allocation);

        return TransportAllocationMapper.toResponse(allocation);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER', 'STUDENT')")
    public TransportAllocationResponse getTransportAllocationByUuid(String allocationUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        TransportAllocation allocation = transportAllocationRepository.findByUuidAndCollegeId(allocationUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport allocation not found with UUID: " + allocationUuid));

        return TransportAllocationMapper.toResponse(allocation);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER')")
    public Page<TransportAllocationResponse> getAllTransportAllocations(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<TransportAllocation> allocations = transportAllocationRepository.findAllByCollegeId(collegeId, pageable);

        return allocations.map(TransportAllocationMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER', 'STUDENT')")
    public Page<TransportAllocationResponse> getActiveTransportAllocations(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<TransportAllocation> allocations = transportAllocationRepository.findActiveByCollegeId(collegeId, pageable);

        return allocations.map(TransportAllocationMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER', 'STUDENT')")
    public List<TransportAllocationResponse> getTransportAllocationsByStudent(String studentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        List<TransportAllocation> allocations = transportAllocationRepository.findByStudentUuidAndCollegeId(studentUuid, collegeId);

        return TransportAllocationMapper.toResponseList(allocations);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER', 'STUDENT')")
    public TransportAllocationResponse getActiveTransportAllocationByStudent(String studentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        TransportAllocation allocation = transportAllocationRepository.findActiveByStudentUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("No active transport allocation found for student with UUID: " + studentUuid));

        return TransportAllocationMapper.toResponse(allocation);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER')")
    public Page<TransportAllocationResponse> getTransportAllocationsByRoute(String routeUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate route exists
        transportRouteRepository.findByUuidAndCollegeId(routeUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport route not found with UUID: " + routeUuid));

        Page<TransportAllocation> allocations = transportAllocationRepository.findByRouteUuidAndCollegeId(routeUuid, collegeId, pageable);

        return allocations.map(TransportAllocationMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER')")
    public List<TransportAllocationResponse> getActiveTransportAllocationsByRoute(String routeUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate route exists
        transportRouteRepository.findByUuidAndCollegeId(routeUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport route not found with UUID: " + routeUuid));

        List<TransportAllocation> allocations = transportAllocationRepository.findActiveByRouteUuidAndCollegeId(routeUuid, collegeId);

        return TransportAllocationMapper.toResponseList(allocations);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public TransportAllocationResponse releaseTransportAllocation(String allocationUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        TransportAllocation allocation = transportAllocationRepository.findByUuidAndCollegeId(allocationUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport allocation not found with UUID: " + allocationUuid));

        if (allocation.getReleasedAt() != null) {
            throw new ResourceConflictException("Transport allocation is already released");
        }

        allocation.setReleasedAt(Instant.now());
        allocation = transportAllocationRepository.save(allocation);

        return TransportAllocationMapper.toResponse(allocation);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public Page<TransportAllocationResponse> searchTransportAllocations(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<TransportAllocation> allocations = transportAllocationRepository.searchByCollegeId(collegeId, searchTerm, pageable);

        return allocations.map(TransportAllocationMapper::toResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public void deleteTransportAllocation(String allocationUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        TransportAllocation allocation = transportAllocationRepository.findByUuidAndCollegeId(allocationUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport allocation not found with UUID: " + allocationUuid));

        transportAllocationRepository.delete(allocation);
    }
}

