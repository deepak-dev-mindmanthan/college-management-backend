package org.collegemanagement.config;


import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.collegemanagement.dto.Token;
import org.collegemanagement.entity.Subscription;
import org.collegemanagement.entity.User;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.services.SubscriptionService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

@Component
public class TokenGenerator {

    final JwtEncoder accessTokenEncoder;
    final JwtEncoder refreshTokenEncoder;
    final SubscriptionService subscriptionService;

    public TokenGenerator(JwtEncoder accessTokenEncoder,
                          @Qualifier("jwtRefreshTokenEncoder") JwtEncoder refreshTokenEncoder,
                          SubscriptionService subscriptionService) {
        this.accessTokenEncoder = accessTokenEncoder;
        this.refreshTokenEncoder = refreshTokenEncoder;
        this.subscriptionService = subscriptionService;
    }

    private String createAccessToken(User user, Subscription subscription) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("myapp")
                .issuedAt(now)
                .expiresAt(now.plus(5, ChronoUnit.DAYS))
                .subject(String.valueOf(user.getId()))
                .claim("roles", roles);

        if (subscription != null) {
            claimsBuilder
                    .claim("subscriptionPlan", subscription.getPlan().name())
                    .claim("subscriptionStatus", subscription.getStatus().name())
                    .claim("subscriptionExpiresAt", subscription.getExpiresAt().toString())
                    .claim("subscriptionPriceAmount", subscription.getPriceAmount())
                    .claim("subscriptionCurrency", subscription.getCurrency());
        }
        if (user.getCollege() != null) {
            claimsBuilder.claim("collegeId", user.getCollege().getId());
        }
        if (user.getEmail() != null) {
            claimsBuilder.claim("email", user.getEmail());
        }

        return accessTokenEncoder.encode(JwtEncoderParameters.from(claimsBuilder.build())).getTokenValue();
    }

    private String createRefreshToken(User user, Authentication authentication, Subscription subscription) {
        Instant now = Instant.now();
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .toList();

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("myApp")
                .issuedAt(now)
                .expiresAt(now.plus(30, ChronoUnit.DAYS))
                .subject(String.valueOf(user.getId()))
                .claim("roles", roles);

        if (subscription != null) {
            claimsBuilder
                    .claim("subscriptionPlan", subscription.getPlan().name())
                    .claim("subscriptionStatus", subscription.getStatus().name())
                    .claim("subscriptionExpiresAt", subscription.getExpiresAt().toString())
                    .claim("subscriptionPriceAmount", subscription.getPriceAmount())
                    .claim("subscriptionCurrency", subscription.getCurrency());
        }
        if (user.getCollege() != null) {
            claimsBuilder.claim("collegeId", user.getCollege().getId());
        }
        if (user.getEmail() != null) {
            claimsBuilder.claim("email", user.getEmail());
        }

        return refreshTokenEncoder.encode(JwtEncoderParameters.from(claimsBuilder.build())).getTokenValue();
    }
    public Token createToken(Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User user)) {
            throw new BadCredentialsException(
                    MessageFormat.format("principal {0} is not of User type", authentication.getPrincipal().getClass())
            );
        }

        Subscription subscription = null;
        boolean isSuperAdmin =
                (user.getRoles() != null && user.getRoles().stream().anyMatch(role -> role.getName() == RoleType.ROLE_SUPER_ADMIN))
                        || authentication.getAuthorities().stream().anyMatch(a ->
                        "ROLE_SUPER_ADMIN".equalsIgnoreCase(a.getAuthority()) || "SUPER_ADMIN".equalsIgnoreCase(a.getAuthority()))
                        || user.getCollege() == null; // treat users without a college (e.g., super admins) as exempt
        if (!isSuperAdmin) {
            subscription = subscriptionService.ensureActiveSubscription(user);
        }

        Token tokenDTO = new Token();
        tokenDTO.setUserId(user.getId());
        tokenDTO.setAccessToken(createAccessToken(user, subscription));

        String refreshToken;
        if (authentication.getCredentials() instanceof Jwt jwt) {
            Instant now = Instant.now();
            Instant expiresAt = jwt.getExpiresAt();
            Duration duration = Duration.between(now, expiresAt);
            long daysUntilExpired = duration.toDays();
            if (daysUntilExpired < 7) {
                refreshToken = createRefreshToken(user, authentication, subscription);
            } else {
                refreshToken = jwt.getTokenValue();
            }
        } else {
            refreshToken = createRefreshToken(user, authentication, subscription);
        }
        tokenDTO.setRefreshToken(refreshToken);
        if (subscription != null) {
            tokenDTO.setSubscriptionPlan(subscription.getPlan());
            tokenDTO.setSubscriptionStatus(subscription.getStatus());
            tokenDTO.setSubscriptionExpiresAt(subscription.getExpiresAt());
            tokenDTO.setSubscriptionPriceAmount(subscription.getPriceAmount());
            tokenDTO.setSubscriptionCurrency(subscription.getCurrency());
        }

        return tokenDTO;
    }

}
