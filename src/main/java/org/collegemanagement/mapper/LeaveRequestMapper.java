package org.collegemanagement.mapper;

import org.collegemanagement.dto.leave.LeaveRequestResponse;
import org.collegemanagement.entity.leave.LeaveRequest;

public class LeaveRequestMapper {

    /**
     * Convert LeaveRequest entity to LeaveRequestResponse DTO
     */
    public static LeaveRequestResponse toResponse(LeaveRequest leaveRequest) {
        if (leaveRequest == null) {
            return null;
        }

        return LeaveRequestResponse.builder()
                .uuid(leaveRequest.getUuid())
                .userUuid(leaveRequest.getUser() != null ? leaveRequest.getUser().getUuid() : null)
                .userName(leaveRequest.getUser() != null ? leaveRequest.getUser().getName() : null)
                .ownerType(leaveRequest.getOwnerType())
                .leaveType(leaveRequest.getLeaveType())
                .startDate(leaveRequest.getStartDate())
                .endDate(leaveRequest.getEndDate())
                .status(leaveRequest.getStatus())
                .approvedByUserUuid(leaveRequest.getApprovedBy() != null ? leaveRequest.getApprovedBy().getUuid() : null)
                .approvedByName(leaveRequest.getApprovedBy() != null ? leaveRequest.getApprovedBy().getName() : null)
                .reason(leaveRequest.getReason())
                .approverComment(leaveRequest.getApproverComment())
                .collegeId(leaveRequest.getUser() != null && leaveRequest.getUser().getCollege() != null
                        ? leaveRequest.getUser().getCollege().getId() : null)
                .build();
    }
}

