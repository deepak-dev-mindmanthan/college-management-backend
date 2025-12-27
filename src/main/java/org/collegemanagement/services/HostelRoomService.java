package org.collegemanagement.services;

import org.collegemanagement.dto.hostel.CreateHostelRoomRequest;
import org.collegemanagement.dto.hostel.HostelRoomResponse;
import org.collegemanagement.dto.hostel.UpdateHostelRoomRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HostelRoomService {

    /**
     * Create a new hostel room
     */
    HostelRoomResponse createHostelRoom(CreateHostelRoomRequest request);

    /**
     * Update hostel room information
     */
    HostelRoomResponse updateHostelRoom(String roomUuid, UpdateHostelRoomRequest request);

    /**
     * Get hostel room by UUID
     */
    HostelRoomResponse getHostelRoomByUuid(String roomUuid);

    /**
     * Get all hostel rooms with pagination
     */
    Page<HostelRoomResponse> getAllHostelRooms(Pageable pageable);

    /**
     * Get all hostel rooms by hostel UUID with pagination
     */
    Page<HostelRoomResponse> getHostelRoomsByHostel(String hostelUuid, Pageable pageable);

    /**
     * Get all hostel rooms by hostel UUID (without pagination)
     */
    List<HostelRoomResponse> getAllHostelRoomsByHostel(String hostelUuid);

    /**
     * Search hostel rooms by room number or hostel name
     */
    Page<HostelRoomResponse> searchHostelRooms(String searchTerm, Pageable pageable);

    /**
     * Delete hostel room by UUID
     */
    void deleteHostelRoom(String roomUuid);
}

