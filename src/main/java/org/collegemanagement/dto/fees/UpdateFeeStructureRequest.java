package org.collegemanagement.dto.fees;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeeStructureRequest {

    @NotEmpty(message = "At least one fee component is required")
    @Valid
    private List<FeeComponentRequest> components;
}

