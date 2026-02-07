package org.collegemanagement.dto.fees;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateFeeStructureRequest {

    @NotEmpty(message = "At least one fee component is required")
    @Valid
    private List<FeeComponentRequest> components;

    private LocalDate dueDate;

    @Valid
    private List<FeeInstallmentTemplateRequest> installments;
}

