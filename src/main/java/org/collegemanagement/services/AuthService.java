package org.collegemanagement.services;

import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.dto.*;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    UserDto registerSuperAdmin(RegisterSuperAdminRequest request);

    UserDto registerCollegeTenant(RegisterCollegeRequest request);

    LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest);


}
