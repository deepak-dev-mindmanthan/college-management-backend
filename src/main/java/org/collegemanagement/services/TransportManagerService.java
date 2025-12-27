package org.collegemanagement.services;

import org.collegemanagement.dto.transport.CreateTransportManagerRequest;
import org.collegemanagement.dto.transport.TransportManagerResponse;
import org.collegemanagement.dto.transport.UpdateTransportManagerRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransportManagerService {

    /**
     * Create a new transport manager
     */
    TransportManagerResponse createTransportManager(CreateTransportManagerRequest request);

    /**
     * Update transport manager information
     */
    TransportManagerResponse updateTransportManager(String transportManagerUuid, UpdateTransportManagerRequest request);

    /**
     * Get transport manager by UUID
     */
    TransportManagerResponse getTransportManagerByUuid(String transportManagerUuid);

    /**
     * Get all transport managers with pagination
     */
    Page<TransportManagerResponse> getAllTransportManagers(Pageable pageable);

    /**
     * Search transport managers by name or email
     */
    Page<TransportManagerResponse> searchTransportManagers(String searchTerm, Pageable pageable);

    /**
     * Delete transport manager by UUID
     */
    void deleteTransportManager(String transportManagerUuid);
}

