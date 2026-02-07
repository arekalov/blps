package com.arekalov.blps.service

import com.arekalov.blps.dto.auth.AuthResponse
import com.arekalov.blps.dto.auth.LoginRequest
import com.arekalov.blps.dto.auth.RegisterRequest
import com.arekalov.blps.exception.UnauthorizedException
import com.arekalov.blps.exception.ValidationException
import com.arekalov.blps.mapper.toAuthResponse
import com.arekalov.blps.mapper.toEntity
import com.arekalov.blps.repository.UserRepository
import com.arekalov.blps.security.JwtTokenProvider
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider,
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ValidationException("User with email ${request.email} already exists")
        }

        val encodedPassword = passwordEncoder.encode(request.password)
        val user = request.toEntity(encodedPassword)
        val savedUser = userRepository.save(user)

        val accessToken = jwtTokenProvider.generateAccessToken(savedUser.id, savedUser.role.name)
        val refreshToken = jwtTokenProvider.generateRefreshToken(savedUser.id)

        return savedUser.toAuthResponse(accessToken, refreshToken)
    }

    @Transactional(readOnly = true)
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw UnauthorizedException("Invalid email or password")

        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw UnauthorizedException("Invalid email or password")
        }

        val accessToken = jwtTokenProvider.generateAccessToken(user.id, user.role.name)
        val refreshToken = jwtTokenProvider.generateRefreshToken(user.id)

        return user.toAuthResponse(accessToken, refreshToken)
    }

    @Transactional(readOnly = true)
    fun refreshToken(refreshToken: String): AuthResponse {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw UnauthorizedException("Invalid or expired refresh token")
        }

        val userId = jwtTokenProvider.getUserIdFromToken(refreshToken)
        val user = userRepository.findById(userId).orElseThrow {
            UnauthorizedException("User not found")
        }

        val newAccessToken = jwtTokenProvider.generateAccessToken(user.id, user.role.name)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(user.id)

        return user.toAuthResponse(newAccessToken, newRefreshToken)
    }
}
