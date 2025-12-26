package org.collegemanagement.dto.admission;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.AdmissionStatus;
import org.collegemanagement.enums.Gender;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdmissionResponse {

    private String uuid;
    private String applicationNo;
    private String studentName;
    private LocalDate dob;
    private Gender gender;
    private String email;
    private String phone;
    private String classUuid;
    private String className;
    private String section;
    private String previousSchool;
    private String documentsJson;
    private AdmissionStatus status;
    private Instant submittedAt;
    private Long collegeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

