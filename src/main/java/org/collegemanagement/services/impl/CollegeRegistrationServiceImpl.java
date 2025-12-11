package org.collegemanagement.services.impl;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.collegemanagement.dto.*;
import org.collegemanagement.entity.College;
import org.collegemanagement.entity.User;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.enums.Status;
import org.collegemanagement.security.jwt.TokenGenerator;
import org.collegemanagement.services.CollegeRegistrationService;
import org.collegemanagement.services.CollegeService;
import org.collegemanagement.services.RoleService;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.stream.Collectors;


@Service
@Transactional
@AllArgsConstructor
public class CollegeRegistrationServiceImpl implements CollegeRegistrationService {

    private final CollegeService collegeService;
    private final SubscriptionService subscriptionService;
    private final RoleService roleService;
    final private UserDetailsManager userDetailsManager;
    final private TokenGenerator tokenGenerator;

    @Override
    public Token registerCollegeTenant(TenantSignUpRequest request) {
        CollegeDto collegeDto = CollegeDto.builder()
                .name(request.getCollegeName())
                .email(request.getCollegeEmail())
                .phone(request.getCollegePhone())
                .address(request.getCollegeAddress())
                .status(Status.ACTIVE)
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
        return tokenGenerator.createToken(authentication);
    }
}
