package org.collegemanagement.controllers;

import org.collegemanagement.config.TokenGenerator;
import org.collegemanagement.dto.CollegeDto;
import org.collegemanagement.dto.LoginRequest;
import org.collegemanagement.dto.SignUpRequest;
import org.collegemanagement.dto.SubscriptionDto;
import org.collegemanagement.dto.SubscriptionRequest;
import org.collegemanagement.dto.TenantSignUpRequest;
import org.collegemanagement.dto.Token;
import org.collegemanagement.entity.College;
import org.collegemanagement.entity.User;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.RoleService;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    final private UserDetailsManager userDetailsManager;
    final private TokenGenerator tokenGenerator;
    final private DaoAuthenticationProvider daoAuthenticationProvider;
    final private JwtAuthenticationProvider refreshTokenAuthProvider;
    private final RoleService roleService;
    private final CollegeService collegeService;
    private final SubscriptionService subscriptionService;

    public AuthController(UserDetailsManager userDetailsManager,
                          TokenGenerator tokenGenerator,
                          DaoAuthenticationProvider daoAuthenticationProvider,
                          @Qualifier("jwtRefreshTokenAuthProvider") JwtAuthenticationProvider refreshTokenAuthProvider,
                          RoleService roleService,
                          CollegeService collegeService,
                          SubscriptionService subscriptionService) {
        this.userDetailsManager = userDetailsManager;
        this.tokenGenerator = tokenGenerator;
        this.daoAuthenticationProvider = daoAuthenticationProvider;
        this.refreshTokenAuthProvider = refreshTokenAuthProvider;
        this.roleService = roleService;
        this.collegeService = collegeService;
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/register")
    public ResponseEntity<Token> register(@RequestBody SignUpRequest signUpRequest) {
        if (userDetailsManager.userExists(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest().build();
        }
        User user = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(signUpRequest.getPassword())
                .roles(roleService.getRoles(RoleType.ROLE_SUPER_ADMIN))
                .build();
        userDetailsManager.createUser(user);
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();
        Collection<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.toUpperCase()))
                .collect(Collectors.toSet());
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(user, signUpRequest.getPassword(), authorities);
        return ResponseEntity.ok(tokenGenerator.createToken(authentication));
    }

    @PostMapping("/register/college")
    public ResponseEntity<?> registerCollegeTenant(@RequestBody TenantSignUpRequest request) {
        if (collegeService.existsByName(request.getCollegeName())) {
            return ResponseEntity.badRequest().body("College with this name already exists.");
        }
        if (collegeService.findByEmail(request.getCollegeEmail()) != null) {
            return ResponseEntity.badRequest().body("College with this email already exists.");
        }
        if (userDetailsManager.userExists(request.getAdminEmail())) {
            return ResponseEntity.badRequest().body("Admin email already used.");
        }

        CollegeDto collegeDto = CollegeDto.builder()
                .name(request.getCollegeName())
                .email(request.getCollegeEmail())
                .phone(request.getCollegePhone())
                .address(request.getCollegeAddress())
                .build();

        CollegeDto createdCollege = collegeService.create(collegeDto);
        College collegeEntity = collegeService.findByEmail(createdCollege.getEmail());

        SubscriptionDto subscription = SubscriptionDto.fromEntity(
                subscriptionService.createOrUpdateForCollege(
                        collegeEntity,
                        SubscriptionRequest.builder()
                                .plan(request.getSubscriptionPlan())
                                .billingCycle(request.getBillingCycle())
                                .build()
                )
        );
        createdCollege.setSubscription(subscription);

        User collegeAdmin = User.builder()
                .name(request.getAdminName())
                .email(request.getAdminEmail())
                .password(request.getAdminPassword())
                .roles(roleService.getRoles(RoleType.ROLE_COLLEGE_ADMIN))
                .college(collegeEntity)
                .build();

        userDetailsManager.createUser(collegeAdmin);

        Collection<GrantedAuthority> authorities = collegeAdmin.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name().toUpperCase()))
                .collect(Collectors.toSet());
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(collegeAdmin, request.getAdminPassword(), authorities);
        return ResponseEntity.ok(tokenGenerator.createToken(authentication));
    }


    @PostMapping("/login")
    public ResponseEntity<Token> login(@RequestBody LoginRequest loginRequest) {
        Authentication authentication = daoAuthenticationProvider.authenticate(UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.getEmail(), loginRequest.getPassword()));
        return ResponseEntity.ok(tokenGenerator.createToken(authentication));
    }

    @PostMapping("/token")
    public ResponseEntity<Token> token(@RequestBody Token tokenDTO) {
        Authentication authentication = refreshTokenAuthProvider.authenticate(new BearerTokenAuthenticationToken(tokenDTO.getRefreshToken()));
        Jwt jwt = (Jwt) authentication.getCredentials();
        //TODO: check if present in db and not revoked, etc
        return ResponseEntity.ok(tokenGenerator.createToken(authentication));
    }



}
