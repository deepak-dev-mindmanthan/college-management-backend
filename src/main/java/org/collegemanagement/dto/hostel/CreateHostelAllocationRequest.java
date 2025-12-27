package org.collegemanagement.dto.hostel;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHostelAllocationRequest {

    @NotBlank(message = "Student UUID is required")
    private String studentUuid;

    @NotBlank(message = "Room UUID is required")
    private String roomUuid;

    private Instant allocatedAt; // Optional, will default to current time if not provided
}

