package com.arekalov.blps.web

import com.arekalov.blps.dto.auth.RegisterRequest
import com.arekalov.blps.exception.ValidationException
import com.arekalov.blps.service.AuthService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Controller
@RequestMapping("/public")
class PublicRegisterController(
    private val authService: AuthService,
) {

    @PostMapping("/register")
    fun register(
        @RequestParam email: String,
        @RequestParam password: String,
        @RequestParam companyName: String,
    ): String {
        val trimmedEmail = email.trim()
        return try {
            authService.register(
                RegisterRequest(
                    email = trimmedEmail,
                    password = password,
                    companyName = companyName.trim(),
                ),
            )
            "redirect:/welcome.html?registered=${encode(trimmedEmail)}"
        } catch (ex: ValidationException) {
            "redirect:/welcome.html?error=${encode(ex.message ?: "validation")}&email=${encode(trimmedEmail)}"
        }
    }

    private fun encode(value: String): String =
        URLEncoder.encode(value, StandardCharsets.UTF_8)
}
