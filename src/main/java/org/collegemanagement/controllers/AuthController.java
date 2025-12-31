package org.collegemanagement.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.dto.*;
import org.collegemanagement.services.AuthService;
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


    @Operation(
            summary = "Register Super Admin",
            description = "Registers a new super admin user"
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User registered successfully.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class)
                    )
            ),
    })

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserDto>> register(@Valid @RequestBody RegisterSuperAdminRequest registerSuperAdminRequest) {
        return ResponseEntity.ok(ApiResponse.success(
                authService.registerSuperAdmin(registerSuperAdminRequest),
                "User registered successfully."
        ));
    }


    @Operation(
            summary = "Register college",
            description = "Register new  college"
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "College registered successfully.",
                    content = @Content(schema = @Schema(implementation = UserDto.class))
            ),
    })
    @PostMapping("/register-college")
    public ResponseEntity<ApiResponse<UserDto>> registerCollegeTenant(@Valid @RequestBody RegisterCollegeRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        authService.registerCollegeTenant(request),
                        "College registered successfully."
                )
        );
    }


    @Operation(
            summary = "Login",
            description = "Login users ",
            security = {}
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successfully.",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
    })
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
