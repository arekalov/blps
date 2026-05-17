package com.arekalov.blps.camunda

import org.camunda.bpm.engine.impl.identity.ReadOnlyIdentityProvider
import org.camunda.bpm.engine.impl.interceptor.Session
import org.camunda.bpm.engine.impl.interceptor.SessionFactory

class BlpsIdentityProviderFactory(
    private val provider: BlpsIdentityProvider,
) : SessionFactory {

    override fun getSessionType(): Class<*> = ReadOnlyIdentityProvider::class.java

    override fun openSession(): Session = provider
}
