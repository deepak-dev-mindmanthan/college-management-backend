package org.collegemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.enums.AttendanceStatus;

import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRequest {
    private Long teacherId;
    private Long subjectId;
    private Long studentId;
    private LocalDate date;
    private AttendanceStatus status;
}
