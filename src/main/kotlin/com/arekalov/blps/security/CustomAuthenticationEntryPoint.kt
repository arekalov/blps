package com.arekalov.blps.security

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping
import java.time.LocalDateTime

@Component
class CustomAuthenticationEntryPoint(
    private val handlerMapping: RequestMappingHandlerMapping,
    private val objectMapper: ObjectMapper,
) : AuthenticationEntryPoint {

    private val logger = LoggerFactory.getLogger(CustomAuthenticationEntryPoint::class.java)

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException,
    ) {
        val handler = try {
            handlerMapping.getHandler(request)
        } catch (e: IllegalStateException) {
            logger.debug("Failed to get handler for ${request.requestURI}", e)
            null
        }

        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"

        if (handler == null) {
            response.status = HttpServletResponse.SC_NOT_FOUND
            val errorResponse = mapOf(
                "timestamp" to LocalDateTime.now().toString(),
                "status" to 404,
                "error" to "Not Found",
                "message" to "Endpoint not found: ${request.method} ${request.requestURI}",
                "path" to request.requestURI,
            )
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
        } else {
            response.status = HttpServletResponse.SC_UNAUTHORIZED
            val errorResponse = mapOf(
                "timestamp" to LocalDateTime.now().toString(),
                "status" to 401,
                "error" to "Unauthorized",
                "message" to "Authentication required",
                "path" to request.requestURI,
            )
            response.writer.write(objectMapper.writeValueAsString(errorResponse))
        }
    }
}
