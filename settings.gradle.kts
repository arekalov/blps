rootProject.name = "blps"

pluginManagement {
	plugins {
		kotlin("jvm") version "2.0.21"
		kotlin("plugin.spring") version "2.0.21"
		kotlin("plugin.jpa") version "2.0.21"
		id("org.springframework.boot") version "3.3.5"
		id("io.spring.dependency-management") version "1.1.7"
		id("io.gitlab.arturbosch.detekt") version "1.23.8"
	}
}

include("main-service", "worker-service", "bitrix-eis-gateway")
