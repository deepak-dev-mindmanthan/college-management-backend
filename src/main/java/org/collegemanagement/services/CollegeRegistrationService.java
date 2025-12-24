package org.collegemanagement.services;

import org.collegemanagement.dto.RegisterCollegeRequest;
import org.collegemanagement.dto.UserDto;
import org.springframework.security.core.Authentication;

public interface CollegeRegistrationService {

        UserDto registerCollegeTenant(RegisterCollegeRequest request);

}
