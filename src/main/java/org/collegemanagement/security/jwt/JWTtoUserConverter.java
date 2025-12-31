package org.collegemanagement.security.jwt;


import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.entity.tenant.College;
import org.collegemanagement.entity.user.Role;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.enums.RoleType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Component
public class JWTtoUserConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt source) {
        User user = new User();
        user.setId(Long.parseLong(source.getSubject()));
        user.setEmail(source.getClaim("email"));
        // Extract roles from JWT claims
        List<String> rolesFromJwt = source.getClaimAsStringList("roles");
        if (rolesFromJwt == null) rolesFromJwt = List.of();

        // Convert roles to a collection of GrantedAuthority
        Set<Role> roleEntities = rolesFromJwt.stream()
                .map(role -> {
                    Role r = new Role();
                    r.setName(RoleType.valueOf(role));
                    return r;
                })
                .collect(Collectors.toSet());
        user.setRoles(roleEntities);

        Collection<GrantedAuthority> authorities = roleEntities.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name().toUpperCase()))
                .collect(Collectors.toSet());

        Object collegeIdClaim = source.getClaim("collegeId");

        if (collegeIdClaim != null && !collegeIdClaim.toString().isBlank()) {
            Long collegeId = Long.valueOf(collegeIdClaim.toString());

            College college = College.builder()
                    .id(collegeId)
                    .build();
            user.setCollege(college);
        }

        return new UsernamePasswordAuthenticationToken(user, source, authorities);
    }


}
