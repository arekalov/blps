package com.arekalov.blps.security

import com.arekalov.blps.jaas.RolePrincipal
import com.arekalov.blps.jaas.UserIdPrincipal
import com.arekalov.blps.model.enum.UserRole
import org.springframework.security.authentication.jaas.JaasAuthenticationToken
import org.springframework.security.core.Authentication
import java.util.UUID
import javax.security.auth.Subject

fun getCurrentUserId(authentication: Authentication?): UUID? {
    val subject = getSubject(authentication) ?: return null
    return subject.principals
        .filterIsInstance<UserIdPrincipal>()
        .firstOrNull()
        ?.userId
}

fun getCurrentUserRole(authentication: Authentication?): UserRole {
    val subject = getSubject(authentication) ?: return UserRole.EMPLOYER
    val roleName = subject.principals
        .filterIsInstance<RolePrincipal>()
        .firstOrNull()
        ?.role
        ?: return UserRole.EMPLOYER
    return when {
        roleName == "ROLE_ADMIN" -> UserRole.ADMIN
        else -> UserRole.EMPLOYER
    }
}

private fun getSubject(authentication: Authentication?): Subject? {
    val token = authentication as? JaasAuthenticationToken ?: return null
    return token.loginContext?.subject
}
