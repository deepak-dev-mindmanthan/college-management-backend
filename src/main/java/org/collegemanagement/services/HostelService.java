package org.collegemanagement.services;

import org.collegemanagement.dto.hostel.CreateHostelRequest;
import org.collegemanagement.dto.hostel.HostelResponse;
import org.collegemanagement.dto.hostel.UpdateHostelRequest;
import org.collegemanagement.enums.HostelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HostelService {

    /**
     * Create a new hostel
     */
    HostelResponse createHostel(CreateHostelRequest request);

    /**
     * Update hostel information
     */
    HostelResponse updateHostel(String hostelUuid, UpdateHostelRequest request);

    /**
     * Get hostel by UUID
     */
    HostelResponse getHostelByUuid(String hostelUuid);

    /**
     * Get all hostels with pagination
     */
    Page<HostelResponse> getAllHostels(Pageable pageable);

    /**
     * Get all hostels (without pagination)
     */
    List<HostelResponse> getAllHostels();

    /**
     * Get hostels by type with pagination
     */
    Page<HostelResponse> getHostelsByType(HostelType type, Pageable pageable);

    /**
     * Search hostels by name
     */
    Page<HostelResponse> searchHostels(String searchTerm, Pageable pageable);

    /**
     * Delete hostel by UUID
     */
    void deleteHostel(String hostelUuid);
}

