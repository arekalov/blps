package com.arekalov.blps.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    @Suppress("LongMethod")
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("BLPS - Job Vacancy Management API")
                    .version("1.0.0")
                    .description(
                        """
                        REST API for job vacancy management system (hh.ru-like) with moderation workflow.
                        
                        ## Features
                        - User registration and HTTP Basic authentication
                        - Vacancy management (CRUD operations)
                        - Tariff selection and vacancy publishing (BPMN workflow)
                        - Vacancy moderation system
                        - Tariff usage statistics and history
                        - Role-based access control (EMPLOYER, MODERATOR, ADMIN)
                        
                        ## User Roles
                        
                        ### EMPLOYER
                        - Create, edit, and delete own vacancies
                        - Select tariff for vacancies
                        - Submit vacancies for moderation
                        - View own vacancy statistics
                        - Update own profile
                        
                        ### MODERATOR
                        - View all pending vacancies
                        - Approve or reject vacancies with reason
                        - View tariff usage statistics
                        - View moderation history
                        - Update own profile
                        
                        ### ADMIN
                        - All EMPLOYER and MODERATOR permissions
                        - Manage users (view, update, delete)
                        - Manage tariffs (create, update, delete)
                        - Full access to all system resources
                        
                        ## Workflow (BPMN)
                        1. Employer creates vacancy (DRAFT status)
                        2. Employer selects tariff
                        3. Employer submits for publication (PENDING_MODERATION status)
                        4. Moderator reviews vacancy
                        5. Moderator approves (PUBLISHED) or rejects (REJECTED) with reason
                        6. If approved: vacancy published + tariff usage recorded in history
                        7. Employer can archive published vacancy (ARCHIVED status)
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
                        "basicAuth",
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("basic")
                            .description("HTTP Basic: email and password"),
                    ),
            )
            .addSecurityItem(SecurityRequirement().addList("basicAuth"))
    }
}
