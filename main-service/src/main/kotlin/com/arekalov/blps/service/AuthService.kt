package com.arekalov.blps.service

import com.arekalov.blps.dto.auth.RegisterRequest
import com.arekalov.blps.dto.user.UserResponse
import com.arekalov.blps.exception.ValidationException
import com.arekalov.blps.mapper.toEntity
import com.arekalov.blps.mapper.toResponse
import com.arekalov.blps.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    fun register(request: RegisterRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ValidationException("User with email ${request.email} already exists")
        }
        val encodedPassword = passwordEncoder.encode(request.password)!!
        val user = request.toEntity(encodedPassword)
        val saved = userRepository.save(user)
        return saved.toResponse()
    }
}
