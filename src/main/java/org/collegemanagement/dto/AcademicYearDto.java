package org.collegemanagement.dto;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class AcademicYearDto {

    private String uuid;
    private String yearName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean active;
}

