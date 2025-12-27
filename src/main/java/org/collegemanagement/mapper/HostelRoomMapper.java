package org.collegemanagement.mapper;

import org.collegemanagement.dto.hostel.HostelRoomResponse;
import org.collegemanagement.entity.hostel.HostelRoom;

import java.util.List;
import java.util.stream.Collectors;

public final class HostelRoomMapper {

    private HostelRoomMapper() {
    }

    /**
     * Convert HostelRoom entity to HostelRoomResponse
     */
    public static HostelRoomResponse toResponse(HostelRoom room, Integer currentOccupancy) {
        if (room == null) {
            return null;
        }

        int occupancy = currentOccupancy != null ? currentOccupancy : 0;
        int available = room.getCapacity() - occupancy;

        return HostelRoomResponse.builder()
                .uuid(room.getUuid())
                .hostelUuid(room.getHostel() != null ? room.getHostel().getUuid() : null)
                .hostelName(room.getHostel() != null ? room.getHostel().getName() : null)
                .roomNumber(room.getRoomNumber())
                .capacity(room.getCapacity())
                .currentOccupancy(occupancy)
                .availableSpots(available)
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    /**
     * Convert HostelRoom entity to HostelRoomResponse (without occupancy info)
     */
    public static HostelRoomResponse toResponse(HostelRoom room) {
        return toResponse(room, 0);
    }

    /**
     * Convert list of HostelRoom entities to list of HostelRoomResponse
     */
    public static List<HostelRoomResponse> toResponseList(List<HostelRoom> rooms) {
        if (rooms == null) {
            return List.of();
        }
        return rooms.stream()
                .map(HostelRoomMapper::toResponse)
                .collect(Collectors.toList());
    }
}

