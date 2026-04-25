package com.arekalov.blps.security

import com.arekalov.blps.jaas.BlpsJaasConfiguration
import com.arekalov.blps.jaas.RolePrincipalAuthorityGranter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.jaas.DefaultJaasAuthenticationProvider
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val customAuthenticationEntryPoint: CustomAuthenticationEntryPoint,
    private val customAccessDeniedHandler: CustomAccessDeniedHandler,
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun blpsJaasConfiguration(): BlpsJaasConfiguration = BlpsJaasConfiguration()

    @Bean
    fun jaasAuthenticationProvider(blpsJaasConfiguration: BlpsJaasConfiguration): DefaultJaasAuthenticationProvider {
        val provider = DefaultJaasAuthenticationProvider()
        provider.setConfiguration(blpsJaasConfiguration)
        provider.setLoginContextName(BlpsJaasConfiguration.LOGIN_CONTEXT_NAME)
        provider.setAuthorityGranters(arrayOf(RolePrincipalAuthorityGranter()))
        return provider
    }

    @Bean
    fun authenticationManager(jaasAuthenticationProvider: DefaultJaasAuthenticationProvider): AuthenticationManager =
        ProviderManager(jaasAuthenticationProvider)

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { cors ->
                cors.configurationSource { request ->
                    val config = org.springframework.web.cors.CorsConfiguration()
                    config.allowedOriginPatterns = listOf("*")
                    config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                    config.allowedHeaders = listOf("*")
                    config.allowCredentials = true
                    config.maxAge = 3600L
                    config
                }
            }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers(
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                    ).permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/tariffs/**",
                    ).permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/vacancies",
                        "/api/v1/vacancies/{id}",
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint(customAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler)
            }
            .httpBasic { }

        return http.build()
    }
}
