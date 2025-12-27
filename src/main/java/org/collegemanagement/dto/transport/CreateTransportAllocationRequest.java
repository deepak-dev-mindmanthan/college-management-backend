package org.collegemanagement.dto.transport;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransportAllocationRequest {

    @NotBlank(message = "Student UUID is required")
    private String studentUuid;

    @NotBlank(message = "Route UUID is required")
    private String routeUuid;

    @NotBlank(message = "Pickup point is required")
    @Size(max = 200, message = "Pickup point must not exceed 200 characters")
    private String pickupPoint;

    private Instant allocatedAt; // Optional, will default to current time if not provided
}

