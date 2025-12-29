package org.collegemanagement.dto.leave;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.LeaveOwnerType;
import org.collegemanagement.enums.LeaveStatus;
import org.collegemanagement.enums.LeaveType;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestResponse {

    private String uuid;
    private String userUuid;
    private String userName;
    private LeaveOwnerType ownerType;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private LeaveStatus status;
    private String approvedByUserUuid;
    private String approvedByName;
    private String reason;
    private String approverComment;
    private Long collegeId;
}

