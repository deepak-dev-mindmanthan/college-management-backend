package org.collegemanagement.mapper;

import org.collegemanagement.dto.transport.TransportRouteResponse;
import org.collegemanagement.entity.transport.TransportRoute;

import java.util.List;
import java.util.stream.Collectors;

public final class TransportRouteMapper {

    private TransportRouteMapper() {
    }

    /**
     * Convert TransportRoute entity to TransportRouteResponse
     */
    public static TransportRouteResponse toResponse(TransportRoute transportRoute) {
        if (transportRoute == null) {
            return null;
        }

        return TransportRouteResponse.builder()
                .uuid(transportRoute.getUuid())
                .routeName(transportRoute.getRouteName())
                .vehicleNo(transportRoute.getVehicleNo())
                .driverName(transportRoute.getDriverName())
                .collegeId(transportRoute.getCollege() != null ? transportRoute.getCollege().getId() : null)
                .createdAt(transportRoute.getCreatedAt())
                .updatedAt(transportRoute.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of TransportRoute entities to list of TransportRouteResponse
     */
    public static List<TransportRouteResponse> toResponseList(List<TransportRoute> transportRoutes) {
        if (transportRoutes == null) {
            return List.of();
        }
        return transportRoutes.stream()
                .map(TransportRouteMapper::toResponse)
                .collect(Collectors.toList());
    }
}

