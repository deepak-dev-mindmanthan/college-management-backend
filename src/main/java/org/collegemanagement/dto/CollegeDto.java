package org.collegemanagement.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Data;
import org.collegemanagement.enums.Status;
import org.collegemanagement.view.Views;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CollegeDto {

    /* =====================
       IDENTIFIERS
       ===================== */

    @JsonView(Views.Internal.class)
    private Long id;                 // internal only

    @JsonView(Views.Public.class)
    private String uuid;             // public identifier

    /* =====================
       BASIC INFO
       ===================== */

    @JsonView(Views.Public.class)
    private String name;

    @JsonView(Views.Public.class)
    private String shortCode;

    @JsonView(Views.Public.class)
    private String logoUrl;

    @JsonView(Views.Public.class)
    private String email;

    @JsonView(Views.Public.class)
    private String phone;

    @JsonView(Views.Public.class)
    private String country;

    @JsonView(Views.Public.class)
    private String state;

    @JsonView(Views.Public.class)
    private String city;

    @JsonView(Views.Public.class)
    private String address;

    @JsonView(Views.Public.class)
    private Status status;

    /* =====================
       REAL RELATIONS (DTOs)
       ===================== */

    @JsonView(Views.Public.class)
    private List<DepartmentDto> departments;

    @JsonView(Views.Public.class)
    private List<AcademicYearDto> academicYears;

    /* =====================
       AUDIT
       ===================== */

    @JsonView(Views.Public.class)
    private LocalDateTime createdAt;

    @JsonView(Views.Public.class)
    private LocalDateTime updatedAt;
}
