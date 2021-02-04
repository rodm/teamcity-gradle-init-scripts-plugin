
import com.github.rodm.teamcity.TeamCityPluginExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.2.0" apply false
    id ("com.github.rodm.teamcity-server") version "1.1-beta-2" apply true
    id ("org.sonarqube") version "2.6.1"
}

extra["teamcityVersion"] = project.findProperty("teamcity.api.version") as String? ?: "10.0"

group = "com.github.rodm"
version = "1.0.2"

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

fun Project.teamcity(configuration: TeamCityPluginExtension.() -> Unit) {
    configure(configuration)
}
