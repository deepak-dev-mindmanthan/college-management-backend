package org.collegemanagement.controllers;

import org.collegemanagement.config.TokenGenerator;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.entity.User;
import org.collegemanagement.dto.LoginRequest;

import org.collegemanagement.dto.SignUpRequest;
import org.collegemanagement.dto.Token;
import org.collegemanagement.services.RoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.provisioning.UserDetailsManager;

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

    public AuthController(UserDetailsManager userDetailsManager, TokenGenerator tokenGenerator, DaoAuthenticationProvider daoAuthenticationProvider, @Qualifier("jwtRefreshTokenAuthProvider") JwtAuthenticationProvider refreshTokenAuthProvider, RoleService roleService) {
        this.userDetailsManager = userDetailsManager;
        this.tokenGenerator = tokenGenerator;
        this.daoAuthenticationProvider = daoAuthenticationProvider;
        this.refreshTokenAuthProvider = refreshTokenAuthProvider;
        this.roleService = roleService;
    }

    @PostMapping("/register")
    public ResponseEntity<Token> register(@RequestBody SignUpRequest signUpRequest) {
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
