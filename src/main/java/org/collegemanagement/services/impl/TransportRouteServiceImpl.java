package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.transport.CreateTransportRouteRequest;
import org.collegemanagement.dto.transport.TransportRouteResponse;
import org.collegemanagement.dto.transport.TransportSummaryResponse;
import org.collegemanagement.dto.transport.UpdateTransportRouteRequest;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.transport.TransportRoute;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.TransportRouteMapper;
import org.collegemanagement.repositories.StudentRepository;
import org.collegemanagement.repositories.TransportAllocationRepository;
import org.collegemanagement.repositories.TransportRouteRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.TransportRouteService;
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
public class TransportRouteServiceImpl implements TransportRouteService {

    private final TransportRouteRepository transportRouteRepository;
    private final TransportAllocationRepository transportAllocationRepository;
    private final StudentRepository studentRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public TransportRouteResponse createTransportRoute(CreateTransportRouteRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Get college and validate tenant access
        College college = getCollegeById(collegeId);

        // Check if route name already exists for this college
        if (transportRouteRepository.existsByRouteNameAndCollegeId(request.getRouteName(), collegeId)) {
            throw new ResourceConflictException("Transport route with name '" + request.getRouteName() + "' already exists in this college");
        }

        // Create transport route
        TransportRoute transportRoute = TransportRoute.builder()
                .college(college)
                .routeName(request.getRouteName())
                .vehicleNo(request.getVehicleNo())
                .driverName(request.getDriverName())
                .build();

        transportRoute = transportRouteRepository.save(transportRoute);

        return TransportRouteMapper.toResponse(transportRoute);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public TransportRouteResponse updateTransportRoute(String routeUuid, UpdateTransportRouteRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find transport route
        TransportRoute transportRoute = transportRouteRepository.findByUuidAndCollegeId(routeUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport route not found with UUID: " + routeUuid));

        // Update route name if provided and validate uniqueness
        if (request.getRouteName() != null && !request.getRouteName().equals(transportRoute.getRouteName())) {
            if (transportRouteRepository.existsByRouteNameAndCollegeIdAndIdNot(request.getRouteName(), collegeId, transportRoute.getId())) {
                throw new ResourceConflictException("Transport route with name '" + request.getRouteName() + "' already exists in this college");
            }
            transportRoute.setRouteName(request.getRouteName());
        }

        // Update vehicle number if provided
        if (request.getVehicleNo() != null) {
            transportRoute.setVehicleNo(request.getVehicleNo());
        }

        // Update driver name if provided
        if (request.getDriverName() != null) {
            transportRoute.setDriverName(request.getDriverName());
        }

        transportRoute = transportRouteRepository.save(transportRoute);

        return TransportRouteMapper.toResponse(transportRoute);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER', 'STUDENT')")
    public TransportRouteResponse getTransportRouteByUuid(String routeUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        TransportRoute transportRoute = transportRouteRepository.findByUuidAndCollegeId(routeUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport route not found with UUID: " + routeUuid));

        return TransportRouteMapper.toResponse(transportRoute);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER', 'STUDENT')")
    public Page<TransportRouteResponse> getAllTransportRoutes(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<TransportRoute> transportRoutes = transportRouteRepository.findAllByCollegeId(collegeId, pageable);

        return transportRoutes.map(TransportRouteMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER', 'TEACHER', 'STUDENT')")
    public List<TransportRouteResponse> getAllTransportRoutes() {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        List<TransportRoute> transportRoutes = transportRouteRepository.findAllByCollegeId(collegeId, Pageable.unpaged()).getContent();

        return transportRoutes.stream()
                .map(TransportRouteMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public Page<TransportRouteResponse> searchTransportRoutes(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<TransportRoute> transportRoutes = transportRouteRepository.searchByCollegeId(collegeId, searchTerm, pageable);

        return transportRoutes.map(TransportRouteMapper::toResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public void deleteTransportRoute(String routeUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        TransportRoute transportRoute = transportRouteRepository.findByUuidAndCollegeId(routeUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Transport route not found with UUID: " + routeUuid));

        // Check if route has any active allocations
        // This will be checked in TransportAllocationRepository
        // For now, we'll allow deletion - the system can track historical data

        transportRouteRepository.delete(transportRoute);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TRANSPORT_MANAGER')")
    public TransportSummaryResponse getTransportSummary() {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        long totalRoutes = transportRouteRepository.countByCollegeId(collegeId);
        long totalActiveAllocations = transportAllocationRepository.countActiveByCollegeId(collegeId);
        long totalAllocations = transportAllocationRepository.findAllByCollegeId(collegeId, org.springframework.data.domain.Pageable.unpaged()).getTotalElements();
        long totalInactiveAllocations = totalAllocations - totalActiveAllocations;
        long totalStudents = studentRepository.countByCollegeId(collegeId);
        long totalStudentsWithTransport = transportAllocationRepository.findActiveByCollegeId(collegeId, org.springframework.data.domain.Pageable.unpaged())
                .getContent()
                .stream()
                .map(ta -> ta.getStudent().getId())
                .distinct()
                .count();
        long totalStudentsWithoutTransport = totalStudents - totalStudentsWithTransport;

        return TransportSummaryResponse.builder()
                .totalRoutes(totalRoutes)
                .totalActiveAllocations(totalActiveAllocations)
                .totalInactiveAllocations(totalInactiveAllocations)
                .totalStudentsWithTransport(totalStudentsWithTransport)
                .totalStudentsWithoutTransport(totalStudentsWithoutTransport)
                .totalStudents(totalStudents)
                .build();
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
    }
}

