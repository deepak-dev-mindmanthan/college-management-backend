package org.collegemanagement.mapper;

import org.collegemanagement.dto.transport.TransportAllocationResponse;
import org.collegemanagement.entity.transport.TransportAllocation;

import java.util.List;
import java.util.stream.Collectors;

public final class TransportAllocationMapper {

    private TransportAllocationMapper() {
    }

    /**
     * Convert TransportAllocation entity to TransportAllocationResponse
     */
    public static TransportAllocationResponse toResponse(TransportAllocation allocation) {
        if (allocation == null) {
            return null;
        }

        var student = allocation.getStudent();
        var route = allocation.getRoute();

        return TransportAllocationResponse.builder()
                .uuid(allocation.getUuid())
                .studentUuid(student != null ? student.getUuid() : null)
                .studentName(student != null && student.getUser() != null ? student.getUser().getName() : null)
                .rollNumber(student != null ? student.getRollNumber() : null)
                .routeUuid(route != null ? route.getUuid() : null)
                .routeName(route != null ? route.getRouteName() : null)
                .vehicleNo(route != null ? route.getVehicleNo() : null)
                .driverName(route != null ? route.getDriverName() : null)
                .pickupPoint(allocation.getPickupPoint())
                .allocatedAt(allocation.getAllocatedAt())
                .releasedAt(allocation.getReleasedAt())
                .isActive(allocation.getReleasedAt() == null)
                .createdAt(allocation.getCreatedAt())
                .updatedAt(allocation.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of TransportAllocation entities to list of TransportAllocationResponse
     */
    public static List<TransportAllocationResponse> toResponseList(List<TransportAllocation> allocations) {
        if (allocations == null) {
            return List.of();
        }
        return allocations.stream()
                .map(TransportAllocationMapper::toResponse)
                .collect(Collectors.toList());
    }
}

