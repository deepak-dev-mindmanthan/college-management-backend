package org.collegemanagement.dto.leave;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.LeaveStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveLeaveRequestRequest {

    @NotNull(message = "Status is required (APPROVED or REJECTED)")
    private LeaveStatus status;

    private String approverComment;
}

