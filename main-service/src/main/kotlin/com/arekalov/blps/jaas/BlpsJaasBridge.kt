package com.arekalov.blps.jaas

import com.arekalov.blps.repository.UserRepository
import jakarta.annotation.PostConstruct
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class BlpsJaasBridge(
    val userRepository: UserRepository,
    val passwordEncoder: PasswordEncoder,
) {
    @PostConstruct
    fun init() {
        instance = this
    }

    companion object {
        @Volatile
        var instance: BlpsJaasBridge? = null
            private set

        fun get(): BlpsJaasBridge =
            instance ?: throw IllegalStateException("BlpsJaasBridge not initialized (Spring context not ready)")
    }
}
