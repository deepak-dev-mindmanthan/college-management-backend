package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.hostel.CreateHostelRoomRequest;
import org.collegemanagement.dto.hostel.HostelRoomResponse;
import org.collegemanagement.dto.hostel.UpdateHostelRoomRequest;
import org.collegemanagement.entity.hostel.Hostel;
import org.collegemanagement.entity.hostel.HostelRoom;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.HostelRoomMapper;
import org.collegemanagement.repositories.HostelAllocationRepository;
import org.collegemanagement.repositories.HostelRepository;
import org.collegemanagement.repositories.HostelRoomRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.HostelRoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HostelRoomServiceImpl implements HostelRoomService {

    private final HostelRoomRepository hostelRoomRepository;
    private final HostelRepository hostelRepository;
    private final HostelAllocationRepository hostelAllocationRepository;
    private final TenantAccessGuard tenantAccessGuard;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelRoomResponse createHostelRoom(CreateHostelRoomRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find hostel
        Hostel hostel = hostelRepository.findByUuidAndCollegeId(request.getHostelUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with UUID: " + request.getHostelUuid()));

        // Check if room number already exists for this hostel
        if (hostelRoomRepository.existsByHostelUuidAndRoomNumberAndCollegeId(request.getHostelUuid(), request.getRoomNumber(), collegeId)) {
            throw new ResourceConflictException("Room number '" + request.getRoomNumber() + "' already exists in this hostel");
        }

        // Create hostel room
        HostelRoom room = HostelRoom.builder()
                .hostel(hostel)
                .roomNumber(request.getRoomNumber())
                .capacity(request.getCapacity())
                .build();

        room = hostelRoomRepository.save(room);

        // Calculate current occupancy
        long currentOccupancyLong = hostelAllocationRepository.countActiveByRoomUuidAndCollegeId(room.getUuid(), collegeId);
        int currentOccupancy = (int) currentOccupancyLong;

        return HostelRoomMapper.toResponse(room, currentOccupancy);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelRoomResponse updateHostelRoom(String roomUuid, UpdateHostelRoomRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find room
        HostelRoom room = hostelRoomRepository.findByUuidAndCollegeId(roomUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel room not found with UUID: " + roomUuid));

        // Update room number if provided and validate uniqueness
        if (request.getRoomNumber() != null && !request.getRoomNumber().equals(room.getRoomNumber())) {
            if (hostelRoomRepository.existsByHostelUuidAndRoomNumberAndCollegeIdAndIdNot(
                    room.getHostel().getUuid(), request.getRoomNumber(), collegeId, room.getId())) {
                throw new ResourceConflictException("Room number '" + request.getRoomNumber() + "' already exists in this hostel");
            }
            room.setRoomNumber(request.getRoomNumber());
        }

        // Update capacity if provided
        if (request.getCapacity() != null) {
            // Check if new capacity is less than current occupancy
            long currentOccupancyLong = hostelAllocationRepository.countActiveByRoomUuidAndCollegeId(room.getUuid(), collegeId);
            int currentOccupancy = (int) currentOccupancyLong;
            if (request.getCapacity() < currentOccupancy) {
                throw new ResourceConflictException("New capacity (" + request.getCapacity() + ") cannot be less than current occupancy (" + currentOccupancy + ")");
            }
            room.setCapacity(request.getCapacity());
        }

        room = hostelRoomRepository.save(room);

        // Calculate current occupancy
        long currentOccupancyLong = hostelAllocationRepository.countActiveByRoomUuidAndCollegeId(room.getUuid(), collegeId);
        int currentOccupancy = (int) currentOccupancyLong;

        return HostelRoomMapper.toResponse(room, currentOccupancy);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public HostelRoomResponse getHostelRoomByUuid(String roomUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        HostelRoom room = hostelRoomRepository.findByUuidAndCollegeId(roomUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel room not found with UUID: " + roomUuid));

        // Calculate current occupancy
        long currentOccupancyLong = hostelAllocationRepository.countActiveByRoomUuidAndCollegeId(room.getUuid(), collegeId);
        int currentOccupancy = (int) currentOccupancyLong;

        return HostelRoomMapper.toResponse(room, currentOccupancy);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public Page<HostelRoomResponse> getAllHostelRooms(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<HostelRoom> rooms = hostelRoomRepository.findAllByCollegeId(collegeId, pageable);

        return rooms.map(room -> {
            long currentOccupancyLong = hostelAllocationRepository.countActiveByRoomUuidAndCollegeId(room.getUuid(), collegeId);
            int currentOccupancy = (int) currentOccupancyLong;
            return HostelRoomMapper.toResponse(room, currentOccupancy);
        });
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public Page<HostelRoomResponse> getHostelRoomsByHostel(String hostelUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate hostel exists
        hostelRepository.findByUuidAndCollegeId(hostelUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with UUID: " + hostelUuid));

        Page<HostelRoom> rooms = hostelRoomRepository.findByHostelUuidAndCollegeId(hostelUuid, collegeId, pageable);

        return rooms.map(room -> {
            long currentOccupancyLong = hostelAllocationRepository.countActiveByRoomUuidAndCollegeId(room.getUuid(), collegeId);
            int currentOccupancy = (int) currentOccupancyLong;
            return HostelRoomMapper.toResponse(room, currentOccupancy);
        });
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public List<HostelRoomResponse> getAllHostelRoomsByHostel(String hostelUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate hostel exists
        hostelRepository.findByUuidAndCollegeId(hostelUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with UUID: " + hostelUuid));

        List<HostelRoom> rooms = hostelRoomRepository.findAllByHostelUuidAndCollegeId(hostelUuid, collegeId);

        return rooms.stream()
                .map(room -> {
                    long currentOccupancyLong = hostelAllocationRepository.countActiveByRoomUuidAndCollegeId(room.getUuid(), collegeId);
                    int currentOccupancy = (int) currentOccupancyLong;
                    return HostelRoomMapper.toResponse(room, currentOccupancy);
                })
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public Page<HostelRoomResponse> searchHostelRooms(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<HostelRoom> rooms = hostelRoomRepository.searchByCollegeId(collegeId, searchTerm, pageable);

        return rooms.map(room -> {
            long currentOccupancyLong = hostelAllocationRepository.countActiveByRoomUuidAndCollegeId(room.getUuid(), collegeId);
            int currentOccupancy = (int) currentOccupancyLong;
            return HostelRoomMapper.toResponse(room, currentOccupancy);
        });
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public void deleteHostelRoom(String roomUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        HostelRoom room = hostelRoomRepository.findByUuidAndCollegeId(roomUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel room not found with UUID: " + roomUuid));

        // Check if room has any active allocations
        long activeAllocations = hostelAllocationRepository.countActiveByRoomUuidAndCollegeId(room.getUuid(), collegeId);
        if (activeAllocations > 0) {
            throw new ResourceConflictException("Cannot delete room with active allocations. Please release all students first.");
        }

        hostelRoomRepository.delete(room);
    }
}

