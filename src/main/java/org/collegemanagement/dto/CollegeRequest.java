package org.collegemanagement.dto;


import lombok.Getter;
import lombok.Setter;
import org.collegemanagement.enums.Status;

@Getter
@Setter

public class CollegeRequest {

    private Long id;
    private String collegeName;
    private String collegeEmail;
    private String collegePhone;
    private String collegeAddress;
    private Status status;

    // College Admin details
    private String adminName;
    private String adminEmail;
    private String adminPassword;
}
