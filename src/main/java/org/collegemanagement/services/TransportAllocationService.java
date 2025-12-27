package org.collegemanagement.services;

import org.collegemanagement.dto.transport.CreateTransportAllocationRequest;
import org.collegemanagement.dto.transport.TransportAllocationResponse;
import org.collegemanagement.dto.transport.UpdateTransportAllocationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransportAllocationService {

    /**
     * Create a new transport allocation
     */
    TransportAllocationResponse createTransportAllocation(CreateTransportAllocationRequest request);

    /**
     * Update transport allocation information
     */
    TransportAllocationResponse updateTransportAllocation(String allocationUuid, UpdateTransportAllocationRequest request);

    /**
     * Get transport allocation by UUID
     */
    TransportAllocationResponse getTransportAllocationByUuid(String allocationUuid);

    /**
     * Get all transport allocations with pagination
     */
    Page<TransportAllocationResponse> getAllTransportAllocations(Pageable pageable);

    /**
     * Get all active transport allocations with pagination
     */
    Page<TransportAllocationResponse> getActiveTransportAllocations(Pageable pageable);

    /**
     * Get all transport allocations for a student
     */
    List<TransportAllocationResponse> getTransportAllocationsByStudent(String studentUuid);

    /**
     * Get active transport allocation for a student
     */
    TransportAllocationResponse getActiveTransportAllocationByStudent(String studentUuid);

    /**
     * Get all transport allocations for a route with pagination
     */
    Page<TransportAllocationResponse> getTransportAllocationsByRoute(String routeUuid, Pageable pageable);

    /**
     * Get all active transport allocations for a route
     */
    List<TransportAllocationResponse> getActiveTransportAllocationsByRoute(String routeUuid);

    /**
     * Release/deactivate transport allocation
     */
    TransportAllocationResponse releaseTransportAllocation(String allocationUuid);

    /**
     * Search transport allocations
     */
    Page<TransportAllocationResponse> searchTransportAllocations(String searchTerm, Pageable pageable);

    /**
     * Delete transport allocation by UUID
     */
    void deleteTransportAllocation(String allocationUuid);
}

