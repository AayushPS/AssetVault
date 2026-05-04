package com.Gemini.AssetVault.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI assetVaultOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("AssetVault API")
                        .description("""
                                Internal IT Asset Management System.
                                Manages the complete lifecycle of company assets —
                                procurement, assignment, maintenance, transfer, and retirement.
                                """)
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("AssetVault Team")
                                .email("admin@assetvault.com")));
    }
}
