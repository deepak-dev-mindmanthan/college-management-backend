package org.collegemanagement.dto.discipline;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.DisciplinaryStatus;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDisciplinaryCaseRequest {

    private LocalDate incidentDate;
    private String description;
    private String actionTaken;
    
    @NotNull(message = "Status is required")
    private DisciplinaryStatus status;
}

