package org.collegemanagement.dto;

import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.*;

@Getter
@Setter
public class RegisterCollegeRequest {

    @NotBlank
    private String collegeName;

    @Email
    @NotBlank
    private String collegeEmail;

    @NotBlank
    @Size(min = 10, max = 15)
    private String collegePhone;

    @NotBlank
    @Size(min = 2, max = 20)
    @Pattern(regexp = "^[A-Z0-9]+$")
    private String collegeShortCode;

    @NotBlank
    private String country;

    /**
     * ADMIN (OWNER)
     */

    @NotBlank
    private String adminName;

    @Email
    @NotBlank
    private String adminEmail;

    @NotBlank
    @Size(min = 8)
    private String password;
}


