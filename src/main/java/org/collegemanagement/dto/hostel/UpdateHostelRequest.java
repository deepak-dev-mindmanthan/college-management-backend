package org.collegemanagement.dto.hostel;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.HostelType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHostelRequest {

    @Size(min = 2, max = 150, message = "Hostel name must be between 2 and 150 characters")
    private String name;

    private HostelType type;

    @Positive(message = "Capacity must be positive")
    private Integer capacity;

    private String wardenUuid; // Can be null to remove warden
}

