package org.collegemanagement.dto;

import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateExamRequest {

    private String name;
    private LocalDate date;
    private Long subjectId;

}
