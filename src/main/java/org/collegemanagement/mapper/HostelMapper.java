package org.collegemanagement.mapper;

import org.collegemanagement.dto.hostel.HostelResponse;
import org.collegemanagement.entity.hostel.Hostel;

import java.util.List;
import java.util.stream.Collectors;

public final class HostelMapper {

    private HostelMapper() {
    }

    /**
     * Convert Hostel entity to HostelResponse
     */
    public static HostelResponse toResponse(Hostel hostel) {
        if (hostel == null) {
            return null;
        }

        return HostelResponse.builder()
                .uuid(hostel.getUuid())
                .name(hostel.getName())
                .type(hostel.getType())
                .capacity(hostel.getCapacity())
                .wardenUuid(hostel.getWarden() != null ? hostel.getWarden().getUuid() : null)
                .wardenName(hostel.getWarden() != null ? hostel.getWarden().getName() : null)
                .collegeId(hostel.getCollege() != null ? hostel.getCollege().getId() : null)
                .createdAt(hostel.getCreatedAt())
                .updatedAt(hostel.getUpdatedAt())
                .build();
    }

    /**
     * Convert list of Hostel entities to list of HostelResponse
     */
    public static List<HostelResponse> toResponseList(List<Hostel> hostels) {
        if (hostels == null) {
            return List.of();
        }
        return hostels.stream()
                .map(HostelMapper::toResponse)
                .collect(Collectors.toList());
    }
}

