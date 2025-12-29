package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.hostel.CreateHostelAllocationRequest;
import org.collegemanagement.dto.hostel.HostelAllocationResponse;
import org.collegemanagement.dto.hostel.HostelSummaryResponse;
import org.collegemanagement.dto.hostel.UpdateHostelAllocationRequest;
import org.collegemanagement.entity.hostel.HostelAllocation;
import org.collegemanagement.entity.hostel.HostelRoom;
import org.collegemanagement.entity.student.ParentStudent;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.AuditAction;
import org.collegemanagement.enums.AuditEntityType;
import org.collegemanagement.enums.NotificationReferenceType;
import org.collegemanagement.enums.NotificationType;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.HostelAllocationMapper;
import org.collegemanagement.repositories.HostelAllocationRepository;
import org.collegemanagement.repositories.HostelRepository;
import org.collegemanagement.repositories.HostelRoomRepository;
import org.collegemanagement.repositories.ParentStudentRepository;
import org.collegemanagement.repositories.StudentRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.AuditService;
import org.collegemanagement.services.HostelAllocationService;
import org.collegemanagement.services.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HostelAllocationServiceImpl implements HostelAllocationService {

    private final HostelAllocationRepository hostelAllocationRepository;
    private final StudentRepository studentRepository;
    private final HostelRoomRepository hostelRoomRepository;
    private final HostelRepository hostelRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final ParentStudentRepository parentStudentRepository;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelAllocationResponse createHostelAllocation(CreateHostelAllocationRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find student
        Student student = studentRepository.findByUuidAndCollegeId(request.getStudentUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + request.getStudentUuid()));

        // Find room
        HostelRoom room = hostelRoomRepository.findByUuidAndCollegeId(request.getRoomUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel room not found with UUID: " + request.getRoomUuid()));

        // Check if student already has an active allocation
        if (hostelAllocationRepository.existsActiveByStudentUuidAndCollegeId(request.getStudentUuid(), collegeId)) {
            throw new ResourceConflictException("Student already has an active hostel allocation");
        }

        // Check if room has available capacity
        long currentOccupancy = hostelAllocationRepository.countActiveByRoomUuidAndCollegeId(request.getRoomUuid(), collegeId);
        if (currentOccupancy >= room.getCapacity()) {
            throw new ResourceConflictException("Room is at full capacity. Current occupancy: " + currentOccupancy + ", Capacity: " + room.getCapacity());
        }

        // Release any existing active allocations for this student (only one active allocation per student)
        List<HostelAllocation> existingActiveAllocations = hostelAllocationRepository
                .findByStudentUuidAndCollegeId(request.getStudentUuid(), collegeId)
                .stream()
                .filter(ha -> ha.getReleasedAt() == null)
                .collect(Collectors.toList());

        for (HostelAllocation existing : existingActiveAllocations) {
            existing.setReleasedAt(Instant.now());
            hostelAllocationRepository.save(existing);
        }

        // Create hostel allocation
        HostelAllocation allocation = HostelAllocation.builder()
                .student(student)
                .room(room)
                .allocatedAt(request.getAllocatedAt() != null ? request.getAllocatedAt() : Instant.now())
                .releasedAt(null)
                .build();

        allocation = hostelAllocationRepository.save(allocation);

        // Create audit log
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            auditService.createAuditLog(
                    currentUser.getId(),
                    AuditAction.CREATE,
                    AuditEntityType.HOSTEL_ALLOCATION,
                    allocation.getId(),
                    "Allocated student " + student.getUser().getName() + " to room " + room.getRoomNumber() + " in " + room.getHostel().getName()
            );
        }

        // Send notifications to student
        if (student.getUser() != null && student.getUser().getId() != null) {
            try {
                notificationService.createNotification(
                        student.getUser().getId(),
                        "Hostel Allocation: " + room.getHostel().getName(),
                        "You have been allocated to " + room.getHostel().getName() + ", Room " + room.getRoomNumber() + ".",
                        NotificationType.IN_APP,
                        NotificationReferenceType.HOSTEL_ALLOCATION,
                        allocation.getId(),
                        "/hostel/allocations/" + allocation.getUuid(),
                        5
                );
            } catch (Exception e) {
                log.warn("Failed to send notification to student: {}", e.getMessage());
            }
        }

        // Send notifications to parents
        List<ParentStudent> parentStudents = parentStudentRepository.findByStudentId(student.getId());
        for (ParentStudent parentStudent : parentStudents) {
            if (parentStudent.getParent() != null && parentStudent.getParent().getUser() != null && parentStudent.getParent().getUser().getId() != null) {
                try {
                    notificationService.createNotification(
                            parentStudent.getParent().getUser().getId(),
                            "Hostel Allocation: " + student.getUser().getName(),
                            "Your child " + student.getUser().getName() + " (" + student.getRollNumber() + ") has been allocated to " + room.getHostel().getName() + ", Room " + room.getRoomNumber() + ".",
                            NotificationType.IN_APP,
                            NotificationReferenceType.HOSTEL_ALLOCATION,
                            allocation.getId(),
                            "/hostel/allocations/" + allocation.getUuid(),
                            5
                    );
                } catch (Exception e) {
                    log.warn("Failed to send notification to parent: {}", e.getMessage());
                }
            }
        }

        return HostelAllocationMapper.toResponse(allocation);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelAllocationResponse updateHostelAllocation(String allocationUuid, UpdateHostelAllocationRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find allocation
        HostelAllocation allocation = hostelAllocationRepository.findByUuidAndCollegeId(allocationUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel allocation not found with UUID: " + allocationUuid));

        // Update room if provided
        if (request.getRoomUuid() != null && !request.getRoomUuid().equals(allocation.getRoom().getUuid())) {
            HostelRoom room = hostelRoomRepository.findByUuidAndCollegeId(request.getRoomUuid(), collegeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Hostel room not found with UUID: " + request.getRoomUuid()));

            // Check if new room has available capacity (only if allocation is active)
            if (allocation.getReleasedAt() == null) {
                long currentOccupancy = hostelAllocationRepository.countActiveByRoomUuidAndCollegeId(request.getRoomUuid(), collegeId);
                if (currentOccupancy >= room.getCapacity()) {
                    throw new ResourceConflictException("Room is at full capacity. Current occupancy: " + currentOccupancy + ", Capacity: " + room.getCapacity());
                }
            }

            allocation.setRoom(room);
        }

        // Update releasedAt if provided (to release/deactivate)
        if (request.getReleasedAt() != null) {
            allocation.setReleasedAt(request.getReleasedAt());
        }

        allocation = hostelAllocationRepository.save(allocation);

        // Create audit log
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            auditService.createAuditLog(
                    currentUser.getId(),
                    AuditAction.UPDATE,
                    AuditEntityType.HOSTEL_ALLOCATION,
                    allocation.getId(),
                    "Updated hostel allocation for student " + allocation.getStudent().getUser().getName()
            );
        }

        return HostelAllocationMapper.toResponse(allocation);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public HostelAllocationResponse getHostelAllocationByUuid(String allocationUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        HostelAllocation allocation = hostelAllocationRepository.findByUuidAndCollegeId(allocationUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel allocation not found with UUID: " + allocationUuid));

        return HostelAllocationMapper.toResponse(allocation);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public Page<HostelAllocationResponse> getAllHostelAllocations(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<HostelAllocation> allocations = hostelAllocationRepository.findAllByCollegeId(collegeId, pageable);

        return allocations.map(HostelAllocationMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public Page<HostelAllocationResponse> getActiveHostelAllocations(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<HostelAllocation> allocations = hostelAllocationRepository.findActiveByCollegeId(collegeId, pageable);

        return allocations.map(HostelAllocationMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public List<HostelAllocationResponse> getHostelAllocationsByStudent(String studentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        List<HostelAllocation> allocations = hostelAllocationRepository.findByStudentUuidAndCollegeId(studentUuid, collegeId);

        return HostelAllocationMapper.toResponseList(allocations);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER', 'STUDENT')")
    public HostelAllocationResponse getActiveHostelAllocationByStudent(String studentUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        HostelAllocation allocation = hostelAllocationRepository.findActiveByStudentUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("No active hostel allocation found for student with UUID: " + studentUuid));

        return HostelAllocationMapper.toResponse(allocation);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public Page<HostelAllocationResponse> getHostelAllocationsByRoom(String roomUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate room exists
        hostelRoomRepository.findByUuidAndCollegeId(roomUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel room not found with UUID: " + roomUuid));

        Page<HostelAllocation> allocations = hostelAllocationRepository.findByRoomUuidAndCollegeId(roomUuid, collegeId, pageable);

        return allocations.map(HostelAllocationMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public List<HostelAllocationResponse> getActiveHostelAllocationsByRoom(String roomUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate room exists
        hostelRoomRepository.findByUuidAndCollegeId(roomUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel room not found with UUID: " + roomUuid));

        List<HostelAllocation> allocations = hostelAllocationRepository.findActiveByRoomUuidAndCollegeId(roomUuid, collegeId);

        return HostelAllocationMapper.toResponseList(allocations);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public Page<HostelAllocationResponse> getHostelAllocationsByHostel(String hostelUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate hostel exists
        hostelRepository.findByUuidAndCollegeId(hostelUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with UUID: " + hostelUuid));

        Page<HostelAllocation> allocations = hostelAllocationRepository.findByHostelUuidAndCollegeId(hostelUuid, collegeId, pageable);

        return allocations.map(HostelAllocationMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER', 'TEACHER')")
    public List<HostelAllocationResponse> getActiveHostelAllocationsByHostel(String hostelUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate hostel exists
        hostelRepository.findByUuidAndCollegeId(hostelUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel not found with UUID: " + hostelUuid));

        List<HostelAllocation> allocations = hostelAllocationRepository.findActiveByHostelUuidAndCollegeId(hostelUuid, collegeId);

        return HostelAllocationMapper.toResponseList(allocations);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelAllocationResponse releaseHostelAllocation(String allocationUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        HostelAllocation allocation = hostelAllocationRepository.findByUuidAndCollegeId(allocationUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel allocation not found with UUID: " + allocationUuid));

        if (allocation.getReleasedAt() != null) {
            throw new ResourceConflictException("Hostel allocation is already released");
        }

        Student student = allocation.getStudent();
        HostelRoom room = allocation.getRoom();

        allocation.setReleasedAt(Instant.now());
        allocation = hostelAllocationRepository.save(allocation);

        // Create audit log
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            auditService.createAuditLog(
                    currentUser.getId(),
                    AuditAction.UPDATE,
                    AuditEntityType.HOSTEL_ALLOCATION,
                    allocation.getId(),
                    "Released hostel allocation for student " + student.getUser().getName() + " from " + room.getHostel().getName() + ", Room " + room.getRoomNumber()
            );
        }

        // Send notifications to student
        if (student.getUser() != null && student.getUser().getId() != null) {
            try {
                notificationService.createNotification(
                        student.getUser().getId(),
                        "Hostel Allocation Released: " + room.getHostel().getName(),
                        "Your hostel allocation has been released from " + room.getHostel().getName() + ", Room " + room.getRoomNumber() + ".",
                        NotificationType.IN_APP,
                        NotificationReferenceType.HOSTEL_ALLOCATION,
                        allocation.getId(),
                        "/hostel/allocations/" + allocation.getUuid(),
                        5
                );
            } catch (Exception e) {
                log.warn("Failed to send notification to student: {}", e.getMessage());
            }
        }

        // Send notifications to parents
        List<ParentStudent> parentStudents = parentStudentRepository.findByStudentId(student.getId());
        for (ParentStudent parentStudent : parentStudents) {
            if (parentStudent.getParent() != null && parentStudent.getParent().getUser() != null && parentStudent.getParent().getUser().getId() != null) {
                try {
                    notificationService.createNotification(
                            parentStudent.getParent().getUser().getId(),
                            "Hostel Allocation Released: " + student.getUser().getName(),
                            "Your child " + student.getUser().getName() + " (" + student.getRollNumber() + ")'s hostel allocation has been released from " + room.getHostel().getName() + ", Room " + room.getRoomNumber() + ".",
                            NotificationType.IN_APP,
                            NotificationReferenceType.HOSTEL_ALLOCATION,
                            allocation.getId(),
                            "/hostel/allocations/" + allocation.getUuid(),
                            5
                    );
                } catch (Exception e) {
                    log.warn("Failed to send notification to parent: {}", e.getMessage());
                }
            }
        }

        return HostelAllocationMapper.toResponse(allocation);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public Page<HostelAllocationResponse> searchHostelAllocations(String searchTerm, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<HostelAllocation> allocations = hostelAllocationRepository.searchByCollegeId(collegeId, searchTerm, pageable);

        return allocations.map(HostelAllocationMapper::toResponse);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public void deleteHostelAllocation(String allocationUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        HostelAllocation allocation = hostelAllocationRepository.findByUuidAndCollegeId(allocationUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Hostel allocation not found with UUID: " + allocationUuid));

        Long allocationId = allocation.getId();
        String studentName = allocation.getStudent().getUser().getName();
        String roomInfo = allocation.getRoom().getRoomNumber() + " in " + allocation.getRoom().getHostel().getName();

        hostelAllocationRepository.delete(allocation);

        // Create audit log
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            auditService.createAuditLog(
                    currentUser.getId(),
                    AuditAction.DELETE,
                    AuditEntityType.HOSTEL_ALLOCATION,
                    allocationId,
                    "Deleted hostel allocation for student " + studentName + " from " + roomInfo
            );
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'HOSTEL_MANAGER')")
    public HostelSummaryResponse getHostelSummary() {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        long totalHostels = hostelRepository.countByCollegeId(collegeId);
        long totalRooms = hostelRoomRepository.countByCollegeId(collegeId);
        long totalActiveAllocations = hostelAllocationRepository.countActiveByCollegeId(collegeId);
        
        // Calculate total allocations (active + inactive)
        long totalAllocations = hostelAllocationRepository.findAllByCollegeId(collegeId, Pageable.unpaged()).getTotalElements();
        long totalInactiveAllocations = totalAllocations - totalActiveAllocations;
        
        long totalStudents = studentRepository.countByCollegeId(collegeId);
        
        // Count unique students with active allocations
        long totalStudentsWithHostel = hostelAllocationRepository.findActiveByCollegeId(collegeId, Pageable.unpaged())
                .getContent()
                .stream()
                .map(ha -> ha.getStudent().getId())
                .distinct()
                .count();
        
        long totalStudentsWithoutHostel = totalStudents - totalStudentsWithHostel;

        // Calculate total capacity and occupancy
        List<org.collegemanagement.entity.hostel.HostelRoom> allRooms = hostelRoomRepository.findAllByCollegeId(collegeId, Pageable.unpaged()).getContent();
        long totalCapacity = allRooms.stream()
                .mapToLong(org.collegemanagement.entity.hostel.HostelRoom::getCapacity)
                .sum();
        
        long totalOccupied = totalActiveAllocations;
        long totalAvailable = totalCapacity - totalOccupied;

        return HostelSummaryResponse.builder()
                .totalHostels(totalHostels)
                .totalRooms(totalRooms)
                .totalActiveAllocations(totalActiveAllocations)
                .totalInactiveAllocations(totalInactiveAllocations)
                .totalStudents(totalStudents)
                .totalStudentsWithHostel(totalStudentsWithHostel)
                .totalStudentsWithoutHostel(totalStudentsWithoutHostel)
                .totalCapacity(totalCapacity)
                .totalOccupied(totalOccupied)
                .totalAvailable(totalAvailable)
                .build();
    }

    private User getCurrentUser() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof User) {
                return (User) principal;
            }
        } catch (Exception e) {
            log.debug("Could not get current user: {}", e.getMessage());
        }
        return null;
    }
}

