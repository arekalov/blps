package com.arekalov.blps.jaas

import org.springframework.security.authentication.jaas.AuthorityGranter
import java.security.Principal

class RolePrincipalAuthorityGranter : AuthorityGranter {

    override fun grant(principal: Principal): Set<String>? {
        if (principal is RolePrincipal) {
            return setOf(principal.role)
        }
        return null
    }
}
