
import com.github.rodm.teamcity.AgentPluginConfiguration
import com.github.rodm.teamcity.AgentPluginDescriptor
import com.github.rodm.teamcity.PluginDeployment
import com.github.rodm.teamcity.TeamCityPluginExtension

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath ("org.junit.platform:junit-platform-gradle-plugin:1.0.0-M4")
    }
}

apply {
    plugin("kotlin")
    plugin("org.gradle.jacoco")
    plugin("org.junit.platform.gradle.plugin")
    plugin("com.github.rodm.teamcity-agent")
}

configurations {
    all {
        exclude(module = "xom")
    }
}

dependencies {
    compile (project(":common"))
    compile (group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version = "1.1.4-3")
    runtime (group = "org.jetbrains.kotlin", name = "kotlin-runtime", version = "1.1.4-3")

    testCompile (group = "junit", name = "junit", version = "4.12")
    testCompile (group = "org.hamcrest", name = "hamcrest-library", version = "1.3")
    testCompile (group = "org.mockito", name = "mockito-core", version = "2.7.22")
    testCompile (group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = "1.1.4-3")
    testCompile ("org.jetbrains.spek:spek-api:1.1.2") {
        exclude (group = "org.jetbrains.kotlin")
    }
    testRuntime ("org.jetbrains.spek:spek-junit-platform-engine:1.1.2") {
        exclude (group = "org.junit.platform")
        exclude (group = "org.jetbrains.kotlin")
    }
}

teamcity {
    agent {
        archiveName = "gradle-init-scripts-agent.zip"
        descriptor(closureOf<AgentPluginDescriptor> {
            pluginDeployment(closureOf<PluginDeployment> {
                useSeparateClassloader = true
            })
        })
    }
}

fun Project.teamcity(configuration: TeamCityPluginExtension.() -> Unit) {
    configure(configuration)
}

fun TeamCityPluginExtension.agent(configuration: AgentPluginConfiguration.() -> Unit) {
    this.agent(closureOf<AgentPluginConfiguration>(configuration))
}
