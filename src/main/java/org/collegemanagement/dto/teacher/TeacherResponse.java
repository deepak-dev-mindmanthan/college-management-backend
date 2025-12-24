package org.collegemanagement.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.Status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponse {

    private String uuid;
    private String name;
    private String email;
    private String designation;
    private BigDecimal salary;
    private LocalDate joiningDate;
    private String phone;
    private String address;
    private Status status;
    private Boolean emailVerified;
    private Instant lastLoginAt;
    private Long collegeId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

