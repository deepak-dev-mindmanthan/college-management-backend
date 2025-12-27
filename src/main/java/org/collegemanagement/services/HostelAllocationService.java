package org.collegemanagement.services;

import org.collegemanagement.dto.hostel.CreateHostelAllocationRequest;
import org.collegemanagement.dto.hostel.HostelAllocationResponse;
import org.collegemanagement.dto.hostel.HostelSummaryResponse;
import org.collegemanagement.dto.hostel.UpdateHostelAllocationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HostelAllocationService {

    /**
     * Create a new hostel allocation
     */
    HostelAllocationResponse createHostelAllocation(CreateHostelAllocationRequest request);

    /**
     * Update hostel allocation information
     */
    HostelAllocationResponse updateHostelAllocation(String allocationUuid, UpdateHostelAllocationRequest request);

    /**
     * Get hostel allocation by UUID
     */
    HostelAllocationResponse getHostelAllocationByUuid(String allocationUuid);

    /**
     * Get all hostel allocations with pagination
     */
    Page<HostelAllocationResponse> getAllHostelAllocations(Pageable pageable);

    /**
     * Get all active hostel allocations with pagination
     */
    Page<HostelAllocationResponse> getActiveHostelAllocations(Pageable pageable);

    /**
     * Get all hostel allocations for a student
     */
    List<HostelAllocationResponse> getHostelAllocationsByStudent(String studentUuid);

    /**
     * Get active hostel allocation for a student
     */
    HostelAllocationResponse getActiveHostelAllocationByStudent(String studentUuid);

    /**
     * Get all hostel allocations for a room with pagination
     */
    Page<HostelAllocationResponse> getHostelAllocationsByRoom(String roomUuid, Pageable pageable);

    /**
     * Get all active hostel allocations for a room
     */
    List<HostelAllocationResponse> getActiveHostelAllocationsByRoom(String roomUuid);

    /**
     * Get all hostel allocations for a hostel with pagination
     */
    Page<HostelAllocationResponse> getHostelAllocationsByHostel(String hostelUuid, Pageable pageable);

    /**
     * Get all active hostel allocations for a hostel
     */
    List<HostelAllocationResponse> getActiveHostelAllocationsByHostel(String hostelUuid);

    /**
     * Release/deactivate hostel allocation
     */
    HostelAllocationResponse releaseHostelAllocation(String allocationUuid);

    /**
     * Search hostel allocations
     */
    Page<HostelAllocationResponse> searchHostelAllocations(String searchTerm, Pageable pageable);

    /**
     * Delete hostel allocation by UUID
     */
    void deleteHostelAllocation(String allocationUuid);

    /**
     * Get hostel summary statistics
     */
    HostelSummaryResponse getHostelSummary();
}

