package org.collegemanagement.config;


import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.collegemanagement.entity.User;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;




@Component
public class JWTtoUserConverter implements Converter<Jwt, UsernamePasswordAuthenticationToken> {

    @Override
    public UsernamePasswordAuthenticationToken convert(Jwt source) {
        User user = new User();
        user.setId(Long.parseLong(source.getSubject()));

        // Extract roles from JWT claims
        List<String> rolesFromJwt = source.getClaimAsStringList("roles");
        if (rolesFromJwt == null) rolesFromJwt = List.of();

        // Convert roles to a collection of GrantedAuthority
        Collection<GrantedAuthority> authorities = rolesFromJwt.stream()
                .map(role -> new SimpleGrantedAuthority(role.toUpperCase()))
                .collect(Collectors.toSet());

        return new UsernamePasswordAuthenticationToken(user, source, authorities);
    }


}
