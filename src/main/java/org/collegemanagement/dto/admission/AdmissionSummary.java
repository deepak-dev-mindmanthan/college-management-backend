package org.collegemanagement.dto.admission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionSummary {
    private long totalApplications;
    private long draftApplications;
    private long submittedApplications;
    private long verifiedApplications;
    private long approvedApplications;
    private long rejectedApplications;
}

