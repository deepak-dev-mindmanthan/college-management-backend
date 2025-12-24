package org.collegemanagement.services.impl;

import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import org.collegemanagement.dto.*;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.enums.Status;
import org.collegemanagement.security.jwt.TokenGenerator;
import org.collegemanagement.services.CollegeRegistrationService;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.RoleService;
import org.collegemanagement.services.UserManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class CollegeRegistrationServiceImpl implements CollegeRegistrationService {

    private final CollegeService collegeService;
    private final RoleService roleService;
    final private UserManager userManager;
    final private TokenGenerator tokenGenerator;

    @Transactional
    @Override
    public UserDto registerCollegeTenant(RegisterCollegeRequest request) {

        College college = College.builder()
                .name(request.getCollegeName())
                .email(request.getCollegeEmail())
                .phone(request.getCollegePhone())
                .shortCode(request.getCollegeShortCode())
                .country(request.getCountry())
                .build();


        CollegeDto createdCollege = collegeService.create(college);

        College collegeEntity = collegeService.findByEmail(createdCollege.getEmail());

        User collegeAdmin = User.builder()
                .email(request.getAdminEmail())
                .name(request.getAdminName())
                .password(request.getPassword())
                .roles(roleService.getRoles(RoleType.ROLE_COLLEGE_ADMIN))
                .college(collegeEntity)
                .build();

//        userDetailsManager.createUser(collegeAdmin);

//        Collection<GrantedAuthority> authorities = collegeAdmin.getRoles().stream()
//                .map(role -> new SimpleGrantedAuthority(role.getName().name().toUpperCase()))
//                .collect(Collectors.toSet());
//        return UsernamePasswordAuthenticationToken.authenticated(collegeAdmin, request.getPassword(), authorities);

        return userManager.createUser(collegeAdmin);

    }


}
