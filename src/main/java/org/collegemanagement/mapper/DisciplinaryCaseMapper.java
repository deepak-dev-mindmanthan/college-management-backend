package org.collegemanagement.mapper;

import org.collegemanagement.dto.discipline.DisciplinaryCaseResponse;
import org.collegemanagement.entity.discipline.DisciplinaryCase;

public class DisciplinaryCaseMapper {

    /**
     * Convert DisciplinaryCase entity to DisciplinaryCaseResponse DTO
     */
    public static DisciplinaryCaseResponse toResponse(DisciplinaryCase disciplinaryCase) {
        if (disciplinaryCase == null) {
            return null;
        }

        return DisciplinaryCaseResponse.builder()
                .uuid(disciplinaryCase.getUuid())
                .studentUuid(disciplinaryCase.getStudent() != null ? disciplinaryCase.getStudent().getUuid() : null)
                .studentName(disciplinaryCase.getStudent() != null && disciplinaryCase.getStudent().getUser() != null
                        ? disciplinaryCase.getStudent().getUser().getName() : null)
                .reportedByUserUuid(disciplinaryCase.getReportedBy() != null ? disciplinaryCase.getReportedBy().getUuid() : null)
                .reportedByName(disciplinaryCase.getReportedBy() != null ? disciplinaryCase.getReportedBy().getName() : null)
                .incidentDate(disciplinaryCase.getIncidentDate())
                .description(disciplinaryCase.getDescription())
                .actionTaken(disciplinaryCase.getActionTaken())
                .status(disciplinaryCase.getStatus())
                .collegeId(disciplinaryCase.getCollege() != null ? disciplinaryCase.getCollege().getId() : null)
                .build();
    }
}

