import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm")
    war
}

group = "com.arekalov"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

dependencies {
    compileOnly("jakarta.servlet:jakarta.servlet-api:6.0.0")
    compileOnly("jakarta.annotation:jakarta.annotation-api:2.1.1")
    compileOnly("jakarta.resource:jakarta.resource-api:2.1.0")
}

tasks.war {
    archiveFileName.set("eis-gateway.war")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

tasks.named<Test>("test") {
    enabled = false
}
