package com.arekalov.blps.jaas

import java.security.Principal
import java.util.UUID

data class EmailPrincipal(val email: String) : Principal {
    override fun getName(): String = email
}

data class RolePrincipal(val role: String) : Principal {
    override fun getName(): String = role
}

data class UserIdPrincipal(val userId: UUID) : Principal {
    override fun getName(): String = userId.toString()
}
