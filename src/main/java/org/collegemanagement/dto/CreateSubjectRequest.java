package org.collegemanagement.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateSubjectRequest {

    private String name;
    private String code;
    private Long courseId;
    private Long teacherId; // Optional

}

