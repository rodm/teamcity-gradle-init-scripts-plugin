
import com.github.rodm.teamcity.TeamCityPluginExtension

plugins {
    kotlin("jvm")
}

apply {
    plugin("org.gradle.jacoco")
    plugin("com.github.rodm.teamcity-agent")
}

configurations {
    all {
        exclude(module = "xom")
    }
}

dependencies {
    compile (project(":common"))
    compile (kotlin("stdlib"))
    runtime (kotlin("runtime"))

    testCompile (kotlin("reflect"))
    testCompile (group = "junit", name = "junit", version = "4.12")
    testCompile (group = "org.hamcrest", name = "hamcrest-library", version = "1.3")
    testCompile (group = "org.mockito", name = "mockito-core", version = "2.7.22")
}

teamcity {
    agent {
        archiveName = "gradle-init-scripts-agent.zip"
        descriptor {
            pluginDeployment {
                useSeparateClassloader = true
            }
        }
    }
}

fun Project.teamcity(configuration: TeamCityPluginExtension.() -> Unit) {
    configure(configuration)
}
