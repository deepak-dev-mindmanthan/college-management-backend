package org.collegemanagement.mapper;

import org.collegemanagement.dto.hostel.HostelAllocationResponse;
import org.collegemanagement.entity.hostel.HostelAllocation;

import java.util.List;
import java.util.stream.Collectors;

public final class HostelAllocationMapper {

    private HostelAllocationMapper() {
    }

    /**
     * Convert HostelAllocation entity to HostelAllocationResponse
     */
    public static HostelAllocationResponse toResponse(HostelAllocation allocation) {
        if (allocation == null) {
            return null;
        }

        var student = allocation.getStudent();
        var room = allocation.getRoom();

        return HostelAllocationResponse.builder()
                .uuid(allocation.getUuid())
                .studentUuid(student != null ? student.getUuid() : null)
                .studentName(student != null && student.getUser() != null ? student.getUser().getName() : null)
                .rollNumber(student != null ? student.getRollNumber() : null)
                .roomUuid(room != null ? room.getUuid() : null)
                .roomNumber(room != null ? room.getRoomNumber() : null)
                .hostelUuid(room != null && room.getHostel() != null ? room.getHostel().getUuid() : null)
                .hostelName(room != null && room.getHostel() != null ? room.getHostel().getName() : null)
                .allocatedAt(allocation.getAllocatedAt())
                .releasedAt(allocation.getReleasedAt())
                .isActive(allocation.getReleasedAt() == null)
                .createdAt(allocation.getCreatedAt())
                .updatedAt(allocation.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of HostelAllocation entities to list of HostelAllocationResponse
     */
    public static List<HostelAllocationResponse> toResponseList(List<HostelAllocation> allocations) {
        if (allocations == null) {
            return List.of();
        }
        return allocations.stream()
                .map(HostelAllocationMapper::toResponse)
                .collect(Collectors.toList());
    }
}

