package org.collegemanagement.security.jwt;


import lombok.AllArgsConstructor;
import org.collegemanagement.entity.user.User;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@AllArgsConstructor
public class TokenGenerator {

    private final JwtEncoder jwtEncoder;

    private static final long ACCESS_TOKEN_DAYS = 5;
    private static final long REFRESH_TOKEN_DAYS = 30;
    private static final String ISSUER = "myapp";

    public String generateAccessToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(ACCESS_TOKEN_DAYS, ChronoUnit.DAYS))
                .subject(String.valueOf(user.getId()))
                .claim("roles", user.getRoles()
                        .stream()
                        .map(r -> r.getName().name())
                        .toList())
                .claim("email", user.getEmail())
                .claim("collegeId",
                        user.getCollege() != null ? user.getCollege().getId():"")
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();
    }

    public String generateRefreshToken(User user) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(ISSUER)
                .issuedAt(now)
                .expiresAt(now.plus(REFRESH_TOKEN_DAYS, ChronoUnit.DAYS))
                .subject(String.valueOf(user.getId()))
                .build();

        return jwtEncoder.encode(
                JwtEncoderParameters.from(claims)
        ).getTokenValue();
    }

    public long getAccessTokenExpirySeconds() {
        return Duration.ofDays(ACCESS_TOKEN_DAYS).toSeconds();
    }

    public long getRefreshTokenExpirySeconds() {
        return Duration.ofDays(REFRESH_TOKEN_DAYS).toSeconds();
    }
}
