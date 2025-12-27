package org.collegemanagement.services;

import org.collegemanagement.dto.hostel.CreateHostelManagerRequest;
import org.collegemanagement.dto.hostel.HostelManagerResponse;
import org.collegemanagement.dto.hostel.UpdateHostelManagerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HostelManagerService {

    /**
     * Create a new hostel manager
     */
    HostelManagerResponse createHostelManager(CreateHostelManagerRequest request);

    /**
     * Update hostel manager information
     */
    HostelManagerResponse updateHostelManager(String hostelManagerUuid, UpdateHostelManagerRequest request);

    /**
     * Get hostel manager by UUID
     */
    HostelManagerResponse getHostelManagerByUuid(String hostelManagerUuid);

    /**
     * Get all hostel managers with pagination
     */
    Page<HostelManagerResponse> getAllHostelManagers(Pageable pageable);

    /**
     * Search hostel managers by name or email
     */
    Page<HostelManagerResponse> searchHostelManagers(String searchTerm, Pageable pageable);

    /**
     * Delete hostel manager by UUID
     */
    void deleteHostelManager(String hostelManagerUuid);
}

