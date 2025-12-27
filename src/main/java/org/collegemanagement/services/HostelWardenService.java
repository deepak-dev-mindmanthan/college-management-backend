package org.collegemanagement.services;

import org.collegemanagement.dto.hostel.CreateHostelWardenRequest;
import org.collegemanagement.dto.hostel.HostelWardenResponse;
import org.collegemanagement.dto.hostel.UpdateHostelWardenRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HostelWardenService {

    /**
     * Create a new hostel warden
     */
    HostelWardenResponse createHostelWarden(CreateHostelWardenRequest request);

    /**
     * Update hostel warden information
     */
    HostelWardenResponse updateHostelWarden(String wardenUuid, UpdateHostelWardenRequest request);

    /**
     * Get hostel warden by UUID
     */
    HostelWardenResponse getHostelWardenByUuid(String wardenUuid);

    /**
     * Get all hostel wardens with pagination
     */
    Page<HostelWardenResponse> getAllHostelWardens(Pageable pageable);

    /**
     * Search hostel wardens by name or email
     */
    Page<HostelWardenResponse> searchHostelWardens(String searchTerm, Pageable pageable);

    /**
     * Get hostels assigned to a warden
     */
    List<org.collegemanagement.dto.hostel.HostelResponse> getHostelsByWarden(String wardenUuid);

    /**
     * Delete hostel warden by UUID
     */
    void deleteHostelWarden(String wardenUuid);
}

