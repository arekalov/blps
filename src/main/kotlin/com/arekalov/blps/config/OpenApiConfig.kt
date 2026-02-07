package com.arekalov.blps.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("BLPS - Job Vacancy Management API")
                    .version("1.0.0")
                    .description(
                        """
                        REST API for job vacancy management system (hh.ru-like).
                        
                        ## Features
                        - User registration and JWT authentication
                        - Vacancy management (CRUD operations)
                        - Tariff selection and vacancy publishing (BPMN workflow)
                        - Role-based access control (EMPLOYER, ADMIN)
                        
                        ## Workflow (BPMN)
                        1. Create vacancy (DRAFT status)
                        2. Select tariff
                        3. Publish vacancy (PUBLISHED status)
                        4. Archive vacancy when needed (ARCHIVED status)
                        """.trimIndent(),
                    )
                    .contact(
                        Contact()
                            .name("BLPS Project")
                            .email("support@blps.example.com"),
                    )
                    .license(
                        License()
                            .name("MIT License")
                            .url("https://opensource.org/licenses/MIT"),
                    ),
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "Bearer Authentication",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("Enter JWT token obtained from /api/v1/auth/login or /api/v1/auth/register"),
                    ),
            )
    }
}
