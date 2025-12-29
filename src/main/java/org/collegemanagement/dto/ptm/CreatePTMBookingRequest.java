package org.collegemanagement.dto.ptm;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePTMBookingRequest {

    @NotBlank(message = "Slot UUID is required")
    private String slotUuid;

    @NotBlank(message = "Student UUID is required")
    private String studentUuid;

    private String remarks;
}

