plugins {
	kotlin("jvm") version "1.9.25"
	kotlin("plugin.spring") version "1.9.25"
	kotlin("plugin.jpa") version "1.9.25"
	id("org.springframework.boot") version "3.3.7"
	id("io.spring.dependency-management") version "1.1.6"
	id("io.gitlab.arturbosch.detekt") version "1.23.8"
	war
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
	implementation("org.springframework.boot:spring-boot-starter-web") {
		exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
	}
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.springframework.boot:spring-boot-starter-security")

	providedRuntime("org.springframework.boot:spring-boot-starter-tomcat")
	compileOnly("jakarta.servlet:jakarta.servlet-api")

	// Только компиляция: на WildFly JSP subsystem отключён; Camunda webapp — static + Spring MVC
	compileOnly("jakarta.servlet.jsp:jakarta.servlet.jsp-api:3.1.1")
	compileOnly("org.glassfish.web:jakarta.servlet.jsp.jstl:3.0.1")

	runtimeOnly("org.postgresql:postgresql")

	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")

	// Camunda 7.22 — Spring Boot 3 / Jakarta EE 10 (WildFly 39)
	implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp:7.22.0") {
		exclude(group = "org.glassfish.hk2", module = "hk2")
	}
	implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-rest:7.22.0") {
		exclude(group = "org.glassfish.hk2", module = "hk2")
		// WildFly + Weld: SpringLifecycleListener тянет CDI-inject ApplicationContext
		exclude(group = "org.glassfish.jersey.ext", module = "jersey-spring6")
	}

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("com.h2database:h2")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
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

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootWar>("bootWar") {
	archiveFileName.set("blps.war")
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}
