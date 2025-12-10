package org.collegemanagement.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class IssueBookRequest {
    private Long bookId;
    private Long studentId;
    private LocalDate dueDate;
}

