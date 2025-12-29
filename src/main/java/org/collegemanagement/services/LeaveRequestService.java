package org.collegemanagement.services;

import org.collegemanagement.dto.leave.ApproveLeaveRequestRequest;
import org.collegemanagement.dto.leave.CreateLeaveRequestRequest;
import org.collegemanagement.dto.leave.LeaveRequestResponse;
import org.collegemanagement.dto.leave.UpdateLeaveRequestRequest;
import org.collegemanagement.enums.LeaveOwnerType;
import org.collegemanagement.enums.LeaveStatus;
import org.collegemanagement.enums.LeaveType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface LeaveRequestService {

    /**
     * Create a new leave request
     */
    LeaveRequestResponse createLeaveRequest(CreateLeaveRequestRequest request);

    /**
     * Update leave request (only if PENDING)
     */
    LeaveRequestResponse updateLeaveRequest(String leaveRequestUuid, UpdateLeaveRequestRequest request);

    /**
     * Approve or reject leave request
     */
    LeaveRequestResponse approveOrRejectLeaveRequest(String leaveRequestUuid, ApproveLeaveRequestRequest request);

    /**
     * Cancel leave request (only if PENDING or APPROVED)
     */
    void cancelLeaveRequest(String leaveRequestUuid);

    /**
     * Get leave request by UUID
     */
    LeaveRequestResponse getLeaveRequestByUuid(String leaveRequestUuid);

    /**
     * Get all leave requests with pagination
     */
    Page<LeaveRequestResponse> getAllLeaveRequests(Pageable pageable);

    /**
     * Get leave requests by user UUID
     */
    Page<LeaveRequestResponse> getLeaveRequestsByUser(String userUuid, Pageable pageable);

    /**
     * Get leave requests by owner type (STUDENT or STAFF)
     */
    Page<LeaveRequestResponse> getLeaveRequestsByOwnerType(LeaveOwnerType ownerType, Pageable pageable);

    /**
     * Get leave requests by status
     */
    Page<LeaveRequestResponse> getLeaveRequestsByStatus(LeaveStatus status, Pageable pageable);

    /**
     * Get leave requests by user UUID and status
     */
    Page<LeaveRequestResponse> getLeaveRequestsByUserAndStatus(String userUuid, LeaveStatus status, Pageable pageable);

    /**
     * Get leave requests by date range
     */
    Page<LeaveRequestResponse> getLeaveRequestsByDateRange(LocalDate startDate, LocalDate endDate, Pageable pageable);

    /**
     * Get leave requests by leave type
     */
    Page<LeaveRequestResponse> getLeaveRequestsByLeaveType(LeaveType leaveType, Pageable pageable);
}

