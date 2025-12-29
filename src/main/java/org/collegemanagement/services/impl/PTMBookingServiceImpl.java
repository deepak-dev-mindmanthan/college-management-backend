package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.ptm.CreatePTMBookingRequest;
import org.collegemanagement.dto.ptm.PTMBookingResponse;
import org.collegemanagement.entity.ptm.PTMBooking;
import org.collegemanagement.entity.ptm.PTMSlot;
import org.collegemanagement.entity.student.Parent;
import org.collegemanagement.entity.student.Student;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.NotificationType;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.PTMBookingMapper;
import org.collegemanagement.repositories.*;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.NotificationService;
import org.collegemanagement.services.PTMBookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PTMBookingServiceImpl implements PTMBookingService {

    private final PTMBookingRepository ptmBookingRepository;
    private final PTMSlotRepository ptmSlotRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final CollegeService collegeService;
    private final StudentRepository studentRepository;
    private final ParentRepository parentRepository;
    private final ParentStudentRepository parentStudentRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'PARENT')")
    public PTMBookingResponse createPTMBooking(CreatePTMBookingRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate tenant access
        getCollegeById(collegeId);

        // Find slot
        PTMSlot slot = ptmSlotRepository.findByUuidAndCollegeId(request.getSlotUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("PTM slot not found with UUID: " + request.getSlotUuid()));

        // Validate slot is active
        if (!slot.getActive()) {
            throw new ResourceConflictException("PTM slot is not active. Cannot book an inactive slot.");
        }

        // Check if slot is already booked
        if (ptmBookingRepository.existsBySlotId(slot.getId())) {
            throw new ResourceConflictException("PTM slot is already booked.");
        }

        // Find student
        Student student = studentRepository.findByUuidAndCollegeId(request.getStudentUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + request.getStudentUuid()));

        // Get current user (parent)
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Validate that current user is a parent
        Parent parent = parentRepository.findByUserIdAndCollegeId(currentUser.getId(), collegeId)
                .orElseThrow(() -> new ResourceConflictException(
                        "Current user is not a parent. Only parents can book PTM slots."));

        // Validate that the parent is associated with the student
        if (!parentStudentRepository.existsByParentIdAndStudentId(parent.getId(), student.getId())) {
            throw new ResourceConflictException(
                    "You are not authorized to book PTM for this student. The student must be associated with your parent account.");
        }

        // Create booking
        PTMBooking booking = PTMBooking.builder()
                .slot(slot)
                .parent(currentUser) // PTMBooking uses User, not Parent entity
                .student(student)
                .bookedAt(java.time.Instant.now())
                .remarks(request.getRemarks())
                .build();

        booking = ptmBookingRepository.save(booking);

        // Send notification to teacher
        if (slot.getParentUser() != null && slot.getParentUser().getId() != null) {
            try {
                notificationService.createNotification(
                        slot.getParentUser().getId(),
                        "PTM Booking: " + student.getUser().getName(),
                        parent.getUser().getName() + " has booked a PTM slot for " + student.getUser().getName()
                                + " on " + slot.getDate() + " from " + slot.getStartTime() + " to " + slot.getEndTime() + ".",
                        NotificationType.IN_APP,
                        null, // NotificationReferenceType doesn't have PTM_BOOKING yet
                        booking.getId(),
                        "/ptm-bookings/" + booking.getUuid(),
                        8 // Medium-high priority
                );
            } catch (Exception e) {
                log.warn("Failed to send notification to teacher: {}", e.getMessage());
            }
        }

        // Send notification to parent (confirming the booking)
        try {
            notificationService.createNotification(
                    currentUser.getId(),
                    "PTM Booking Confirmed",
                    "Your PTM booking for " + student.getUser().getName() + " has been confirmed for "
                            + slot.getDate() + " from " + slot.getStartTime() + " to " + slot.getEndTime() + ".",
                    NotificationType.IN_APP,
                    null,
                    booking.getId(),
                    "/ptm-bookings/" + booking.getUuid(),
                    5
            );
        } catch (Exception e) {
            log.warn("Failed to send notification to parent: {}", e.getMessage());
        }

        return PTMBookingMapper.toResponse(booking);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'PARENT')")
    public void cancelPTMBooking(String bookingUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find booking
        PTMBooking booking = ptmBookingRepository.findByUuidAndCollegeId(bookingUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("PTM booking not found with UUID: " + bookingUuid));

        // Get current user
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Validate that current user is the parent who made the booking or an admin
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().equals("ROLE_SUPER_ADMIN") || role.getName().equals("ROLE_COLLEGE_ADMIN"));
        boolean isParentOwner = booking.getParent().getId().equals(currentUser.getId());

        if (!isAdmin && !isParentOwner) {
            throw new ResourceConflictException("You are not authorized to cancel this booking.");
        }

        // Delete booking
        ptmBookingRepository.delete(booking);

        // Send notification to teacher
        if (booking.getSlot() != null && booking.getSlot().getParentUser() != null
                && booking.getSlot().getParentUser().getId() != null) {
            try {
                notificationService.createNotification(
                        booking.getSlot().getParentUser().getId(),
                        "PTM Booking Cancelled: " + booking.getStudent().getUser().getName(),
                        "PTM booking for " + booking.getStudent().getUser().getName() + " on "
                                + booking.getSlot().getDate() + " has been cancelled.",
                        NotificationType.IN_APP,
                        null,
                        null, // No reference ID after deletion
                        "/ptm-slots",
                        5
                );
            } catch (Exception e) {
                log.warn("Failed to send notification to teacher: {}", e.getMessage());
            }
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public PTMBookingResponse getPTMBookingByUuid(String bookingUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        PTMBooking booking = ptmBookingRepository.findByUuidAndCollegeId(bookingUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("PTM booking not found with UUID: " + bookingUuid));

        return PTMBookingMapper.toResponse(booking);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<PTMBookingResponse> getAllPTMBookings(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<PTMBooking> bookings = ptmBookingRepository.findAllByCollegeId(collegeId, pageable);

        return bookings.map(PTMBookingMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'PARENT')")
    public Page<PTMBookingResponse> getPTMBookingsByParent(String parentUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate parent exists (for admins, allow any parent UUID; for parents, validate it's their own)
        User currentUser = getCurrentUser();
        if (currentUser != null && !currentUser.getUuid().equals(parentUuid)) {
            // Check if user is admin
            boolean isAdmin = currentUser.getRoles().stream()
                    .anyMatch(role -> role.getName().equals("ROLE_SUPER_ADMIN") || role.getName().equals("ROLE_COLLEGE_ADMIN"));
            if (!isAdmin) {
                // For non-admins, only allow viewing their own bookings
                if (!currentUser.getUuid().equals(parentUuid)) {
                    throw new ResourceConflictException("You can only view your own PTM bookings.");
                }
            }
        }

        Page<PTMBooking> bookings = ptmBookingRepository.findByParentUuidAndCollegeId(parentUuid, collegeId, pageable);

        return bookings.map(PTMBookingMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'PARENT', 'STUDENT')")
    public Page<PTMBookingResponse> getPTMBookingsByStudent(String studentUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate student exists
        studentRepository.findByUuidAndCollegeId(studentUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with UUID: " + studentUuid));

        Page<PTMBooking> bookings = ptmBookingRepository.findByStudentUuidAndCollegeId(studentUuid, collegeId, pageable);

        return bookings.map(PTMBookingMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<PTMBookingResponse> getPTMBookingsByTeacher(String teacherUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<PTMBooking> bookings = ptmBookingRepository.findByTeacherUuidAndCollegeId(teacherUuid, collegeId, pageable);

        return bookings.map(PTMBookingMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<PTMBookingResponse> getPTMBookingsByDate(java.time.LocalDate date, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<PTMBooking> bookings = ptmBookingRepository.findByDateAndCollegeId(date, collegeId, pageable);

        return bookings.map(PTMBookingMapper::toResponse);
    }

    // Helper methods

    private College getCollegeById(Long collegeId) {
        College college = collegeService.findById(collegeId);
        tenantAccessGuard.assertCurrentTenant(college);
        return college;
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

