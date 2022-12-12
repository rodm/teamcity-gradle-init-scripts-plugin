
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72" apply false
    id ("io.github.rodm.teamcity-base") version "1.5" apply true
    id ("org.sonarqube") version "3.4.0.2513"
}

extra["teamcityVersion"] = project.findProperty("teamcity.api.version") as String? ?: "2018.1"

group = "com.github.rodm"
version = "1.0.3"

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

teamcity {
    version = extra["teamcityVersion"] as String
}
