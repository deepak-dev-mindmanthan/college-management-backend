package org.collegemanagement.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.dto.*;
import org.collegemanagement.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@RequestBody RegisterSuperAdminRequest registerSuperAdminRequest) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.registerSuperAdmin(registerSuperAdminRequest),
                "User registered successfully."
        ));
    }

    @PostMapping("/register-college")
    public ResponseEntity<ApiResponse<UserDto>> registerCollegeTenant(@RequestBody RegisterCollegeRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        authService.registerCollegeTenant(request),
                        "College registered successfully."
                )
        );
    }


    @Operation(
            summary = "Login",
            security = {}
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.login(loginRequest),
                "Login successfully.")
        );
    }


    @Operation(
            summary = "Refresh Token",
            description = "Generates a new access token using refresh token"
    )
    @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200",
            description = "Token refreshed successfully",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
    )
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<LoginResponse>> token(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.refreshToken(refreshTokenRequest)
                , "Token refreshed successfully.")
        );
    }
}
