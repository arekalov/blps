import org.gradle.api.tasks.TaskProvider

plugins {
	base
	kotlin("jvm") apply false
	kotlin("plugin.spring") apply false
	kotlin("plugin.jpa") apply false
	id("org.springframework.boot") apply false
	id("io.spring.dependency-management") apply false
	id("io.gitlab.arturbosch.detekt") apply false
}

subprojects {
	group = "com.arekalov"
	version = "0.0.1-SNAPSHOT"
	repositories {
		mavenCentral()
	}
}

tasks.register("detekt") {
	dependsOn(
		project(":main-service").tasks.named("detekt"),
		project(":worker-service").tasks.named("detekt"),
	)
}

tasks.named("check") {
	dependsOn(subprojects.map { it.tasks.named("check") })
}

tasks.register<Exec>("generateOpenApi") {
	group = "documentation"
	description = "Generate OpenAPI specification from running application (main-service)"
	workingDir(rootDir)
	commandLine("bash", "scripts/generate-openapi.sh")
}

tasks.register("bootRun") {
	dependsOn(":main-service:bootRun")
}

tasks.register("bootJar") {
	dependsOn(":main-service:bootJar", ":worker-service:bootJar")
}
