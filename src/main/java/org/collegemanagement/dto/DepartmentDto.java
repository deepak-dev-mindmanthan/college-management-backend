package org.collegemanagement.dto;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DepartmentDto {

    private String uuid;
    private String name;
    private String code;

    // HOD (safe summary)
    private String headUserUuid;
    private String headUserName;
}

