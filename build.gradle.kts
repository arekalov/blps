plugins {
	kotlin("jvm") version "2.0.21"
	kotlin("plugin.spring") version "2.0.21"
	kotlin("plugin.jpa") version "2.0.21"
	id("org.springframework.boot") version "4.0.2"
	id("io.spring.dependency-management") version "1.1.7"
	id("io.gitlab.arturbosch.detekt") version "1.23.8"
}

group = "com.arekalov"
version = "0.0.1-SNAPSHOT"
description = "Blps lab project"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")
	
	runtimeOnly("org.postgresql:postgresql")
	
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
	
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
	
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	
	detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")
	
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

detekt {
	buildUponDefaultConfig = true
	allRules = false
	config.setFrom("$projectDir/detekt.yml")
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
	reports {
		html.required.set(true)
		html.outputLocation.set(file("build/reports/detekt/detekt.html"))
		txt.required.set(true)
		txt.outputLocation.set(file("build/reports/detekt/detekt.txt"))
		xml.required.set(false)
		sarif.required.set(false)
		md.required.set(false)
	}
	jvmTarget = "17"
}

tasks.named("check") {
	setDependsOn(dependsOn.filterNot { (it as? TaskProvider<*>)?.name == "detekt" })
}

tasks.register<Exec>("generateOpenApi") {
	group = "documentation"
	description = "Generate OpenAPI specification from running application"
	commandLine("bash", "scripts/generate-openapi.sh")
}
