package com.ondrecreates.incidentmanagement.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Incident Management System API",
                version = "v1",
                description = "State machine, SLA tracking and append-only audit trail for "
                        + "incident lifecycle management. JWT issued by identity_server_app "
                        + "(OAuth2 Resource Server) is required on every /api/v1/** endpoint."
        )
)
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {
}
