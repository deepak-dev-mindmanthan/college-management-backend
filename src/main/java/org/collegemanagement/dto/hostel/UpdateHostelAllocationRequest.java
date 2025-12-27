package org.collegemanagement.dto.hostel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHostelAllocationRequest {

    private String roomUuid; // Can change room

    private Instant releasedAt; // Can release/deactivate allocation
}

