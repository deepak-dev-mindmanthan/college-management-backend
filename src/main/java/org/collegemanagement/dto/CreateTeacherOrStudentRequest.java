package org.collegemanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTeacherOrStudentRequest {
    private String name;
    private String email;
    private String password;
    private Long collegeId;
}
