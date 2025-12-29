package org.collegemanagement.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.dto.leave.ApproveLeaveRequestRequest;
import org.collegemanagement.dto.leave.CreateLeaveRequestRequest;
import org.collegemanagement.dto.leave.LeaveRequestResponse;
import org.collegemanagement.dto.leave.UpdateLeaveRequestRequest;
import org.collegemanagement.entity.leave.LeaveRequest;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.LeaveOwnerType;
import org.collegemanagement.enums.LeaveStatus;
import org.collegemanagement.enums.LeaveType;
import org.collegemanagement.enums.NotificationReferenceType;
import org.collegemanagement.enums.NotificationType;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.mapper.LeaveRequestMapper;
import org.collegemanagement.repositories.LeaveRequestRepository;
import org.collegemanagement.repositories.UserRepository;
import org.collegemanagement.security.tenant.TenantAccessGuard;
import org.collegemanagement.services.LeaveRequestService;
import org.collegemanagement.services.NotificationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaveRequestServiceImpl implements LeaveRequestService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final TenantAccessGuard tenantAccessGuard;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public LeaveRequestResponse createLeaveRequest(CreateLeaveRequestRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find user
        User user = userRepository.findByUuidAndCollegeId(request.getUserUuid(), collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + request.getUserUuid()));

        // Validate tenant access
        if (user.getCollege() != null) {
            tenantAccessGuard.assertCurrentTenant(user.getCollege());
        }

        // Validate date range
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new ResourceConflictException("End date cannot be before start date");
        }

        if (request.getStartDate().isBefore(LocalDate.now())) {
            throw new ResourceConflictException("Start date cannot be in the past");
        }

        // Check for overlapping leave requests (only PENDING and APPROVED)
        List<LeaveStatus> overlappingStatuses = Arrays.asList(LeaveStatus.PENDING, LeaveStatus.APPROVED);
        List<LeaveRequest> overlappingLeaves = leaveRequestRepository.findOverlappingLeaves(
                user.getId(),
                request.getStartDate(),
                request.getEndDate(),
                overlappingStatuses,
                -1L // Exclude ID (new request)
        );

        if (!overlappingLeaves.isEmpty()) {
            throw new ResourceConflictException(
                    "You already have a leave request for this date range. Please check your existing leave requests.");
        }

        // Create leave request
        LeaveRequest leaveRequest = LeaveRequest.builder()
                .user(user)
                .ownerType(request.getOwnerType())
                .leaveType(request.getLeaveType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .approvedBy(null)
                .approverComment(null)
                .build();

        leaveRequest = leaveRequestRepository.save(leaveRequest);

        // Send notification to user
        try {
            notificationService.createNotification(
                    user.getId(),
                    "Leave Request Submitted",
                    "Your leave request from " + request.getStartDate() + " to " + request.getEndDate()
                            + " has been submitted and is pending approval.",
                    NotificationType.IN_APP,
                    NotificationReferenceType.LEAVE_REQUEST,
                    leaveRequest.getId(),
                    "/leave-requests/" + leaveRequest.getUuid(),
                    5
            );
        } catch (Exception e) {
            log.warn("Failed to send notification: {}", e.getMessage());
        }

        return LeaveRequestMapper.toResponse(leaveRequest);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public LeaveRequestResponse updateLeaveRequest(String leaveRequestUuid, UpdateLeaveRequestRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find leave request
        LeaveRequest leaveRequest = leaveRequestRepository.findByUuidAndCollegeId(leaveRequestUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with UUID: " + leaveRequestUuid));

        // Only allow updates if status is PENDING
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new ResourceConflictException(
                    "Cannot update leave request. Only PENDING leave requests can be updated.");
        }

        // Update fields
        if (request.getLeaveType() != null) {
            leaveRequest.setLeaveType(request.getLeaveType());
        }
        if (request.getStartDate() != null) {
            leaveRequest.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            leaveRequest.setEndDate(request.getEndDate());
        }
        if (request.getReason() != null) {
            leaveRequest.setReason(request.getReason());
        }

        // Validate date range if dates are being updated
        if (request.getStartDate() != null || request.getEndDate() != null) {
            LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : leaveRequest.getStartDate();
            LocalDate endDate = request.getEndDate() != null ? request.getEndDate() : leaveRequest.getEndDate();

            if (endDate.isBefore(startDate)) {
                throw new ResourceConflictException("End date cannot be before start date");
            }

            if (startDate.isBefore(LocalDate.now())) {
                throw new ResourceConflictException("Start date cannot be in the past");
            }

            // Check for overlapping leave requests (excluding current one)
            List<LeaveStatus> overlappingStatuses = Arrays.asList(LeaveStatus.PENDING, LeaveStatus.APPROVED);
            List<LeaveRequest> overlappingLeaves = leaveRequestRepository.findOverlappingLeaves(
                    leaveRequest.getUser().getId(),
                    startDate,
                    endDate,
                    overlappingStatuses,
                    leaveRequest.getId()
            );

            if (!overlappingLeaves.isEmpty()) {
                throw new ResourceConflictException(
                        "You already have a leave request for this date range. Please check your existing leave requests.");
            }
        }

        leaveRequest = leaveRequestRepository.save(leaveRequest);

        return LeaveRequestMapper.toResponse(leaveRequest);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public LeaveRequestResponse approveOrRejectLeaveRequest(String leaveRequestUuid, ApproveLeaveRequestRequest request) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find leave request
        LeaveRequest leaveRequest = leaveRequestRepository.findByUuidAndCollegeId(leaveRequestUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with UUID: " + leaveRequestUuid));

        // Only allow approval/rejection if status is PENDING
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new ResourceConflictException(
                    "Cannot approve/reject leave request. Only PENDING leave requests can be approved/rejected.");
        }

        // Validate status
        if (request.getStatus() != LeaveStatus.APPROVED && request.getStatus() != LeaveStatus.REJECTED) {
            throw new ResourceConflictException("Status must be either APPROVED or REJECTED");
        }

        // Get current user (approver)
        User approver = getCurrentUser();
        if (approver == null) {
            throw new IllegalStateException("User not authenticated");
        }

        // Update leave request
        leaveRequest.setStatus(request.getStatus());
        leaveRequest.setApprovedBy(approver);
        leaveRequest.setApproverComment(request.getApproverComment());

        leaveRequest = leaveRequestRepository.save(leaveRequest);

        // Send notification to user
        try {
            String statusMessage = request.getStatus() == LeaveStatus.APPROVED
                    ? "Your leave request has been approved."
                    : "Your leave request has been rejected.";
            if (request.getApproverComment() != null && !request.getApproverComment().isBlank()) {
                statusMessage += " Comment: " + request.getApproverComment();
            }

            notificationService.createNotification(
                    leaveRequest.getUser().getId(),
                    "Leave Request " + request.getStatus(),
                    statusMessage,
                    NotificationType.IN_APP,
                    NotificationReferenceType.LEAVE_REQUEST,
                    leaveRequest.getId(),
                    "/leave-requests/" + leaveRequest.getUuid(),
                    5
            );
        } catch (Exception e) {
            log.warn("Failed to send notification: {}", e.getMessage());
        }

        return LeaveRequestMapper.toResponse(leaveRequest);
    }

    @Override
    @Transactional
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public void cancelLeaveRequest(String leaveRequestUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Find leave request
        LeaveRequest leaveRequest = leaveRequestRepository.findByUuidAndCollegeId(leaveRequestUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with UUID: " + leaveRequestUuid));

        // Only allow cancellation if status is PENDING or APPROVED
        if (leaveRequest.getStatus() != LeaveStatus.PENDING && leaveRequest.getStatus() != LeaveStatus.APPROVED) {
            throw new ResourceConflictException(
                    "Cannot cancel leave request. Only PENDING or APPROVED leave requests can be cancelled.");
        }

        // Update status to CANCELLED
        leaveRequest.setStatus(LeaveStatus.CANCELLED);
        leaveRequestRepository.save(leaveRequest);

        // Send notification to user
        try {
            notificationService.createNotification(
                    leaveRequest.getUser().getId(),
                    "Leave Request Cancelled",
                    "Your leave request has been cancelled.",
                    NotificationType.IN_APP,
                    NotificationReferenceType.LEAVE_REQUEST,
                    leaveRequest.getId(),
                    "/leave-requests/" + leaveRequest.getUuid(),
                    5
            );
        } catch (Exception e) {
            log.warn("Failed to send notification: {}", e.getMessage());
        }
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public LeaveRequestResponse getLeaveRequestByUuid(String leaveRequestUuid) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        LeaveRequest leaveRequest = leaveRequestRepository.findByUuidAndCollegeId(leaveRequestUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("Leave request not found with UUID: " + leaveRequestUuid));

        return LeaveRequestMapper.toResponse(leaveRequest);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<LeaveRequestResponse> getAllLeaveRequests(Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<LeaveRequest> leaveRequests = leaveRequestRepository.findAllByCollegeId(collegeId, pageable);

        return leaveRequests.map(LeaveRequestMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public Page<LeaveRequestResponse> getLeaveRequestsByUser(String userUuid, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate user exists
        userRepository.findByUuidAndCollegeId(userUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + userUuid));

        Page<LeaveRequest> leaveRequests = leaveRequestRepository.findByUserUuidAndCollegeId(userUuid, collegeId, pageable);

        return leaveRequests.map(LeaveRequestMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<LeaveRequestResponse> getLeaveRequestsByOwnerType(LeaveOwnerType ownerType, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<LeaveRequest> leaveRequests = leaveRequestRepository.findByOwnerTypeAndCollegeId(ownerType, collegeId, pageable);

        return leaveRequests.map(LeaveRequestMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<LeaveRequestResponse> getLeaveRequestsByStatus(LeaveStatus status, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<LeaveRequest> leaveRequests = leaveRequestRepository.findByStatusAndCollegeId(status, collegeId, pageable);

        return leaveRequests.map(LeaveRequestMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public Page<LeaveRequestResponse> getLeaveRequestsByUserAndStatus(String userUuid, LeaveStatus status, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        // Validate user exists
        userRepository.findByUuidAndCollegeId(userUuid, collegeId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + userUuid));

        Page<LeaveRequest> leaveRequests = leaveRequestRepository.findByUserUuidAndStatusAndCollegeId(
                userUuid, status, collegeId, pageable);

        return leaveRequests.map(LeaveRequestMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<LeaveRequestResponse> getLeaveRequestsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<LeaveRequest> leaveRequests = leaveRequestRepository.findByDateRangeAndCollegeId(
                collegeId, startDate, endDate, pageable);

        return leaveRequests.map(LeaveRequestMapper::toResponse);
    }

    @Override
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public Page<LeaveRequestResponse> getLeaveRequestsByLeaveType(LeaveType leaveType, Pageable pageable) {
        Long collegeId = tenantAccessGuard.getCurrentTenantId();

        Page<LeaveRequest> leaveRequests = leaveRequestRepository.findByLeaveTypeAndCollegeId(leaveType, collegeId, pageable);

        return leaveRequests.map(LeaveRequestMapper::toResponse);
    }

    // Helper methods

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

