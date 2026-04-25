package com.arekalov.blps.jaas

import javax.security.auth.login.AppConfigurationEntry
import javax.security.auth.login.Configuration

class BlpsJaasConfiguration : Configuration() {

    override fun getAppConfigurationEntry(name: String): Array<AppConfigurationEntry>? {
        if (name != LOGIN_CONTEXT_NAME) return null
        return arrayOf(
            AppConfigurationEntry(
                BlpsLoginModule::class.java.name,
                AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                emptyMap<String, Any>(),
            ),
        )
    }

    companion object {
        const val LOGIN_CONTEXT_NAME = "Blps"
    }
}
