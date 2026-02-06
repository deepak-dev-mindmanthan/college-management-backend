package org.collegemanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.media.StringSchema;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;

@Configuration


@OpenAPIDefinition(
        security = {@SecurityRequirement(name = "BearerAuth")}
        ,
        info = @Info(
                title = "College Management SaaS API",
                version = "v1",
                description = """
                        SaaS-based multi-tenant College & School Management System.
                        
                        • UUID-based public APIs
                        • Tenant-isolated data access
                        • JWT authentication
                        • Role-based authorization
                        
                        NOTE: For SUPER_ADMIN role, the 'X-Tenant-ID' header must be provided for tenant-isolated endpoints.
                        """,
                contact = @Contact(
                        name = "MindManthan Software Solutions",
                        email = "support@mindmanthansoftwaresolutions.com"
                )
        ),
        servers = {
                @Server( description = "API v1")
        }
)

@SecurityScheme(
        name = "BearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {

    @Bean
    public OperationCustomizer addTenantHeader() {
        return (operation, handlerMethod) -> {
            // Check if method or class has @PreAuthorize annotation
            boolean hasSecurity = handlerMethod.getMethodAnnotation(PreAuthorize.class) != null ||
                    handlerMethod.getBeanType().getAnnotation(PreAuthorize.class) != null;

            if (hasSecurity) {
                Parameter tenantHeader = new Parameter()
                        .in("header")
                        .name("X-Tenant-ID")
                        .description("Tenant ID (College ID) for SUPER_ADMIN access. Required for tenant-isolated operations when logged in as SUPER_ADMIN.")
                        .required(false)
                        .schema(new StringSchema());
                operation.addParametersItem(tenantHeader);
            }
            return operation;
        };
    }
}
