package com.arekalov.blps.jaas

import javax.security.auth.Subject
import javax.security.auth.callback.CallbackHandler
import javax.security.auth.callback.NameCallback
import javax.security.auth.callback.PasswordCallback
import javax.security.auth.login.LoginException
import javax.security.auth.spi.LoginModule

class BlpsLoginModule : LoginModule {

    private var subject: Subject? = null
    private var callbackHandler: CallbackHandler? = null
    private val addedPrincipals = mutableListOf<java.security.Principal>()

    override fun initialize(
        subject: Subject?,
        callbackHandler: CallbackHandler?,
        sharedState: MutableMap<String, *>?,
        options: Map<String, *>?,
    ) {
        this.subject = subject
        this.callbackHandler = callbackHandler
    }

    override fun login(): Boolean {
        val handler = callbackHandler ?: throw LoginException("No CallbackHandler")
        val nameCallback = NameCallback("Email: ")
        val passwordCallback = PasswordCallback("Password: ", false)
        handler.handle(arrayOf(nameCallback, passwordCallback))

        val email = nameCallback.name ?: throw LoginException("Email is null")
        val password = passwordCallback.password ?: throw LoginException("Password is null")

        val bridge = BlpsJaasBridge.get()
        val user = bridge.userRepository.findByEmail(email)
            ?: throw LoginException("Invalid email or password")

        if (!bridge.passwordEncoder.matches(String(password), user.passwordHash)) {
            throw LoginException("Invalid email or password")
        }

        addedPrincipals.add(EmailPrincipal(user.email))
        addedPrincipals.add(RolePrincipal("ROLE_${user.role.name}"))
        addedPrincipals.add(UserIdPrincipal(user.id!!))
        return true
    }

    override fun commit(): Boolean {
        val s = subject ?: return false
        addedPrincipals.forEach { s.principals.add(it) }
        return true
    }

    override fun abort(): Boolean {
        addedPrincipals.clear()
        return true
    }

    override fun logout(): Boolean {
        subject?.principals?.removeAll(addedPrincipals.toSet())
        addedPrincipals.clear()
        return true
    }
}
