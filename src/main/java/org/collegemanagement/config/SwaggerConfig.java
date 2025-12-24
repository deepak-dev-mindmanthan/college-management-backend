package org.collegemanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

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
                        """,
                contact = @Contact(
                        name = "Mind Manthan Software Solutions",
                        email = "support@mindmanthansoftwaresolutions.com"
                )
        ),
        servers = {
                @Server(url = "/api/v1", description = "API v1")
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
}
