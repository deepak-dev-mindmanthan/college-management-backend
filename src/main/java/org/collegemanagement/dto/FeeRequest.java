package org.collegemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.collegemanagement.enums.FeeStatus;

import java.time.LocalDate;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FeeRequest {
    private Long studentId;
    private Double amount;
    private LocalDate dueDate;
    private FeeStatus status;
}

