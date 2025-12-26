package org.collegemanagement.dto.student;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.Gender;
import org.collegemanagement.enums.Status;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDetailResponse {

    private String uuid;
    private String name;
    private String email;
    private String rollNumber;
    private String registrationNumber;
    private Instant dob;
    private Gender gender;
    private Instant admissionDate;
    private String bloodGroup;
    private String address;
    private Status status;
    private Boolean emailVerified;
    private Instant lastLoginAt;
    private Long collegeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional details
    private List<ParentInfo> parents;
    private List<EnrollmentInfo> enrollments;
    private EnrollmentInfo currentEnrollment;
}

