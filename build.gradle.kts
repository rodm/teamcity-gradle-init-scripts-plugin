
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72" apply false
    id ("com.github.rodm.teamcity-server") version "1.3.2" apply true
    id ("org.sonarqube") version "3.1"
}

extra["teamcityVersion"] = project.findProperty("teamcity.api.version") as String? ?: "2018.1"

group = "com.github.rodm"
version = "1.1-SNAPSHOT"

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
