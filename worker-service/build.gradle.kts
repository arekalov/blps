import org.gradle.api.tasks.TaskProvider

plugins {
	kotlin("jvm")
	kotlin("plugin.spring")
	kotlin("plugin.jpa")
	id("org.springframework.boot")
	id("io.spring.dependency-management")
	id("io.gitlab.arturbosch.detekt")
}

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(17))
	}
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.kafka:spring-kafka")

	runtimeOnly("org.postgresql:postgresql")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.8")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

detekt {
	buildUponDefaultConfig = true
	allRules = false
	config.setFrom(rootProject.file("detekt.yml"))
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
	reports {
		html.required.set(true)
		html.outputLocation.set(layout.buildDirectory.file("reports/detekt/detekt.html"))
		txt.required.set(true)
		txt.outputLocation.set(layout.buildDirectory.file("reports/detekt/detekt.txt"))
		xml.required.set(false)
		sarif.required.set(false)
		md.required.set(false)
	}
	jvmTarget = "17"
}

tasks.named("check") {
	setDependsOn(dependsOn.filterNot { (it as? TaskProvider<*>)?.name == "detekt" })
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	archiveFileName.set("blps-worker.jar")
}
