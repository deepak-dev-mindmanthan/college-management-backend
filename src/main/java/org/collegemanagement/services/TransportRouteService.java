package org.collegemanagement.services;

import org.collegemanagement.dto.transport.CreateTransportRouteRequest;
import org.collegemanagement.dto.transport.TransportRouteResponse;
import org.collegemanagement.dto.transport.UpdateTransportRouteRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface TransportRouteService {

    /**
     * Create a new transport route
     */
    TransportRouteResponse createTransportRoute(CreateTransportRouteRequest request);

    /**
     * Update transport route information
     */
    TransportRouteResponse updateTransportRoute(String routeUuid, UpdateTransportRouteRequest request);

    /**
     * Get transport route by UUID
     */
    TransportRouteResponse getTransportRouteByUuid(String routeUuid);

    /**
     * Get all transport routes with pagination
     */
    Page<TransportRouteResponse> getAllTransportRoutes(Pageable pageable);

    /**
     * Get all transport routes (without pagination)
     */
    List<TransportRouteResponse> getAllTransportRoutes();

    /**
     * Search transport routes by route name, vehicle number, or driver name
     */
    Page<TransportRouteResponse> searchTransportRoutes(String searchTerm, Pageable pageable);

    /**
     * Delete transport route by UUID
     */
    void deleteTransportRoute(String routeUuid);

    /**
     * Get transport summary statistics
     */
    org.collegemanagement.dto.transport.TransportSummaryResponse getTransportSummary();
}

