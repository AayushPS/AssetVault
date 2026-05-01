package com.Gemini.AssetVault.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI assetVaultOpenAPI() {

        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("AssetVault API")
                        .description("""
                                Internal IT Asset Management System.
                                Manages the complete lifecycle of company assets —
                                procurement, assignment, maintenance, transfer, and retirement.
                                
                                Use POST /api/v1/auth/login to obtain a JWT token,
                                then click Authorize and paste: Bearer <your_token>
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("AssetVault Team")
                                .email("admin@assetvault.com")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        ));
    }
}