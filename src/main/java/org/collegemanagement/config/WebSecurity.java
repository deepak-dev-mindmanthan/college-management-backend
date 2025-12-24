package org.collegemanagement.config;


import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.security.filter.TenantIsolationFilter;
import org.collegemanagement.security.handler.CustomAccessDeniedHandler;
import org.collegemanagement.security.handler.CustomAuthenticationEntryPoint;
import org.collegemanagement.security.jwt.JWTtoUserConverter;
import org.collegemanagement.security.permission.CollegeIsolationPermissionEvaluator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@Slf4j
public class WebSecurity {
    final JWTtoUserConverter jwtToUserConverter;
    private final CollegeIsolationPermissionEvaluator permissionEvaluator;

    @Qualifier("jwtRefreshTokenDecoder")
    final JwtDecoder jwtRefreshTokenDecoder;

    final PasswordEncoder passwordEncoder;
    final UserDetailsManager userDetailsManager;
    CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    CustomAccessDeniedHandler customAccessDeniedHandler;

    public WebSecurity(JWTtoUserConverter jwtToUserConverter, CollegeIsolationPermissionEvaluator permissionEvaluator,
                       JwtDecoder jwtRefreshTokenDecoder,
                       PasswordEncoder passwordEncoder,
                       UserDetailsManager userDetailsManager,
                       CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                       CustomAccessDeniedHandler customAccessDeniedHandler
    ) {
        this.jwtToUserConverter = jwtToUserConverter;
        this.permissionEvaluator = permissionEvaluator;
        this.jwtRefreshTokenDecoder = jwtRefreshTokenDecoder;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsManager = userDetailsManager;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/pricing/**").permitAll()
                        .requestMatchers("/api/v1/superuser/**").hasRole("SUPER_ADMIN")
                        .requestMatchers("/api/v1/college-admin/**").hasAnyRole("SUPER_ADMIN", "COLLEGE_ADMIN")
                        .requestMatchers("/swagger-ui.html", "/api/docs", "/api/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .anyRequest().authenticated()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer((oauth2) ->
                        oauth2.authenticationEntryPoint(customAuthenticationEntryPoint)
                                .accessDeniedHandler(customAccessDeniedHandler)
                                .jwt((jwt) -> jwt.jwtAuthenticationConverter(jwtToUserConverter))
                )
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // ⭐ ADD THIS LINE
                .addFilterAfter(
                        tenantIsolationFilter(),
                        UsernamePasswordAuthenticationFilter.class
                );


        return http.build();
    }


    @Bean
    @Qualifier("jwtRefreshTokenAuthProvider")
    JwtAuthenticationProvider jwtRefreshTokenAuthProvider() {
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtRefreshTokenDecoder);
        provider.setJwtAuthenticationConverter(jwtToUserConverter);
        return provider;
    }

    @Bean
    DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsManager);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }


    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("roles");
        JwtAuthenticationConverter converter =
                new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }


    @Bean
    MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler =
                new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }


    @Bean
    public TenantIsolationFilter tenantIsolationFilter() {
        return new TenantIsolationFilter();
    }


}
