package org.collegemanagement.dto.hostel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.collegemanagement.enums.Status;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HostelWardenResponse {

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
    private List<String> assignedHostelUuids; // List of hostel UUIDs assigned to this warden
    private Integer assignedHostelCount; // Count of hostels assigned
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

