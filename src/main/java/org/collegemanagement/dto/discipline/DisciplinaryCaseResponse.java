package org.collegemanagement.dto.discipline;

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
public class DisciplinaryCaseResponse {

    private String uuid;
    private String studentUuid;
    private String studentName;
    private String reportedByUserUuid;
    private String reportedByName;
    private LocalDate incidentDate;
    private String description;
    private String actionTaken;
    private DisciplinaryStatus status;
    private Long collegeId;
}

