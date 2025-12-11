package org.collegemanagement.services;

import org.collegemanagement.dto.TenantSignUpRequest;
import org.collegemanagement.dto.Token;

public interface CollegeRegistrationService {

        public Token registerCollegeTenant(TenantSignUpRequest request);

}
