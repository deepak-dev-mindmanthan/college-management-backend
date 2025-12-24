package org.collegemanagement.services.impl;

import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.dto.*;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.enums.SubscriptionPlanType;
import org.collegemanagement.exception.ResourceConflictException;
import org.collegemanagement.mapper.SubscriptionMapper;
import org.collegemanagement.mapper.UserMapper;
import org.collegemanagement.security.jwt.TokenGenerator;
import org.collegemanagement.services.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.oauth2.core.OAuth2TokenValidatorResult.success;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserManager userManager;
    private final DaoAuthenticationProvider daoAuthenticationProvider;
    private final JwtAuthenticationProvider refreshTokenAuthProvider;
    private final TokenGenerator tokenGenerator;
    private final RoleService roleService;
    private final CollegeService collegeService;
    private final CollegeRegistrationService collegeRegistrationService;
    private final SubscriptionService subscriptionService;

    public AuthServiceImpl(
            UserManager userManager,
            DaoAuthenticationProvider daoAuthenticationProvider,
            @Qualifier("jwtRefreshTokenAuthProvider") JwtAuthenticationProvider refreshTokenAuthProvider,
            TokenGenerator tokenGenerator,
            RoleService roleService,
            CollegeService collegeService,
            CollegeRegistrationService collegeRegistrationService,
            SubscriptionService subscriptionService
    ) {
        this.userManager = userManager;
        this.daoAuthenticationProvider = daoAuthenticationProvider;
        this.refreshTokenAuthProvider = refreshTokenAuthProvider;
        this.tokenGenerator = tokenGenerator;
        this.roleService = roleService;
        this.collegeService = collegeService;
        this.collegeRegistrationService = collegeRegistrationService;
        this.subscriptionService = subscriptionService;
    }

    @Transactional
    @Override
    public UserDto registerSuperAdmin(RegisterSuperAdminRequest request) {

        if (userManager.userExists(request.getEmail())) {
            throw new ResourceConflictException("Email already exists");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword()) // RAW
                .roles(roleService.getRoles(RoleType.ROLE_SUPER_ADMIN))
                .build();

//        userManager.createUser(user);

//        Authentication authentication =
//                UsernamePasswordAuthenticationToken.authenticated(
//                        user,
//                        user.getPassword(), //In production make it null so that non-super admin user cannot create another super admin user and return general info instead of tokens info
//                        user.getAuthorities()
//                );
//
//        if (!(authentication.getPrincipal() instanceof User userData)) {
//            throw new BadCredentialsException("Authentication principal is invalid");
//        }

        return userManager.createUser(user);
    }

    @Transactional
    @Override
    public UserDto registerCollegeTenant(RegisterCollegeRequest request) {

        validateCollegeAndAdminUniqueness(request);

        return collegeRegistrationService.registerCollegeTenant(request);
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        Authentication authentication =
                daoAuthenticationProvider.authenticate(
                        UsernamePasswordAuthenticationToken.unauthenticated(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        if (!(authentication.getPrincipal() instanceof User user)) {
            throw new BadCredentialsException("Authentication principal is invalid");
        }

        return buildLoginResponse(user);
    }

    @Override
    public LoginResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {

        Authentication authentication =
                refreshTokenAuthProvider.authenticate(
                        new BearerTokenAuthenticationToken(refreshTokenRequest.getRefreshToken())
                );

        if (authentication == null) {
            throw new BadCredentialsException("Invalid refresh token");
        }

        Object principal = authentication.getPrincipal();

        User user;

        if (principal instanceof User) {
            user = (User) principal;
            user = userManager.findById(user.getId());
        } else if (principal instanceof Jwt jwt) {
            Long userId = Long.valueOf(jwt.getSubject());
            user = userManager.findById(userId);
        } else {
            throw new BadCredentialsException(
                    "Unsupported authentication principal: " + (principal != null ? principal.getClass() : null)
            );
        }
        return buildLoginResponse(user);
    }

    private LoginResponse buildLoginResponse(User user) {
        return LoginResponse.builder()
                .user(UserMapper.toSummary(user))
                .subscription(
                        isSuperAdmin(user) ? null : subscriptionService.getSubscriptionByCollegeId(user.getCollege() != null ? user.getCollege().getId() : null)
                                .map(SubscriptionMapper::toSummary)
                                .orElse(
                                        SubscriptionSummary.builder()
                                                .plan(SubscriptionPlanType.NONE)
                                                .canAccessCoreApis(false)
                                                .build()
                                )
                )
                .auth(
                        Token.builder()
                                .tokenType("Bearer")
                                .accessToken(tokenGenerator.generateAccessToken(user))
                                .refreshToken(tokenGenerator.generateRefreshToken(user))
                                .accessTokenExpiresIn(
                                        tokenGenerator.getAccessTokenExpirySeconds())
                                .refreshTokenExpiresIn(
                                        tokenGenerator.getRefreshTokenExpirySeconds())
                                .build()
                )
                .build();
    }


    private void validateCollegeAndAdminUniqueness(RegisterCollegeRequest request) {

        if (collegeService.existsByName(request.getCollegeName())) {
            throw new ResourceConflictException(
                    "College name already exists: " + request.getCollegeName()
            );
        }


        if (collegeService.exitsByShortCode(request.getCollegeShortCode())) {
            throw new ResourceConflictException(
                    "College shortCode already used: " + request.getCollegeShortCode()
            );
        }



        if (collegeService.existsByEmail(request.getCollegeEmail())) {
            throw new ResourceConflictException(
                    "College email already exists: " + request.getCollegeEmail()
            );
        }

        if (collegeService.existsByPhone(request.getCollegePhone())) {
            throw new ResourceConflictException(
                    "College phone already exists: " + request.getCollegePhone()
            );
        }

        if (userManager.userExists(request.getAdminEmail())) {
            throw new ResourceConflictException(
                    "Admin email already used: " + request.getAdminEmail()
            );
        }
    }

    private boolean isSuperAdmin(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleType.ROLE_SUPER_ADMIN);
    }

}
