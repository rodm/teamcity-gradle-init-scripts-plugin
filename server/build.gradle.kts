
import com.github.rodm.teamcity.TeamCityEnvironment
import com.github.rodm.teamcity.TeamCityPluginExtension
import com.github.rodm.teamcity.tasks.PublishTask

plugins {
    kotlin("jvm")
}

apply {
    plugin("groovy")
    plugin("org.gradle.jacoco")
    plugin("com.github.rodm.teamcity-server")
}

extra["downloadsDir"] = project.findProperty("downloads.dir") ?: "${rootDir}/downloads"
extra["serversDir"] = project.findProperty("servers.dir") ?: "${rootDir}/servers"
extra["java8Home"] = project.findProperty("java8.home") ?: "/opt/jdk1.8.0_131"

configurations {
    all {
        exclude(module = "xom")
        exclude(module = "slf4j-log4j12")
    }
}

val agent = configurations.getByName("agent")
val provided = configurations.getByName("provided")

dependencies {
    compile (project(":common"))
    compile (kotlin("stdlib"))

    provided (group = "org.jetbrains.teamcity.internal", name = "server", version = rootProject.extra["teamcityVersion"] as String?)

    agent (project(path = ":agent", configuration = "plugin"))

    testCompile (localGroovy())
    testCompile (group = "junit", name = "junit", version = "4.12")
    testCompile (group = "org.hamcrest", name = "hamcrest-library", version = "1.3")
    testCompile (group = "org.mockito", name = "mockito-core", version = "2.7.22")

    testRuntime (kotlin("runtime"))
}

tasks.withType<PublishTask> {
    username = findProperty("jetbrains.username") as String?
    password = findProperty("jetbrains.password") as String?
}

teamcity {
    server {
        archiveName = "gradle-init-scripts-${rootProject.version}.zip"

        descriptor {
            name = "gradleInitScripts"
            displayName = "Gradle Init Scripts"
            version = rootProject.version as String?
            description = "Provides support for reusing Gradle init scripts"
            vendorName = "Rod MacKenzie"
            vendorUrl = "https://github.com/rodm"
            downloadUrl = "https://github.com/rodm/teamcity-gradle-init-scripts-plugin"
            email = "rod.n.mackenzie@gmail.com"
            useSeparateClassloader = true
        }
    }

    environments {
        downloadsDir = extra["downloadsDir"] as String
        baseHomeDir = extra["serversDir"] as String
        baseDataDir = "${rootDir}/data"

        operator fun String.invoke(block: TeamCityEnvironment.() -> Unit) {
            environments.create(this, closureOf<TeamCityEnvironment>(block))
        }

        "teamcity10" {
            version = "10.0.5"
            serverOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
            agentOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006")
        }

        "teamcity2017.1" {
            version = "2017.1.5"
        }

        "teamcity2017.2" {
            version = "2017.2"
        }
    }
}

fun Project.teamcity(configuration: TeamCityPluginExtension.() -> Unit) {
    configure(configuration)
}
