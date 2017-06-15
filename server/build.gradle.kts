
import com.github.rodm.teamcity.ServerPluginConfiguration
import com.github.rodm.teamcity.ServerPluginDescriptor
import com.github.rodm.teamcity.TeamCityEnvironment
import com.github.rodm.teamcity.TeamCityEnvironments
import com.github.rodm.teamcity.TeamCityPluginExtension

apply {
    plugin("kotlin")
    plugin("groovy")
    plugin("org.gradle.jacoco")
    plugin("com.github.rodm.teamcity-server")
}

extra["downloadsDir"] = project.findProperty("downloads.dir") ?: "${rootDir}/downloads"
extra["serversDir"] = project.findProperty("servers.dir") ?: "${rootDir}/servers"
extra["java8Home"] = project.findProperty("java8.home") ?: "/opt/jdk1.8.0_131"

val agent = configurations.getByName("agent")

dependencies {
    compile (project(":common"))
    compile (group = "org.jetbrains.kotlin", name = "kotlin-stdlib", version = "1.1.2-2")

    agent (project(path = ":agent", configuration = "plugin"))

    testCompile (localGroovy())
    testCompile (group = "junit", name = "junit", version = "4.12")
    testCompile (group = "org.hamcrest", name = "hamcrest-library", version = "1.3")
    testCompile (group = "org.mockito", name = "mockito-core", version = "2.7.22")

    testRuntime (group = "org.jetbrains.kotlin", name = "kotlin-runtime", version = "1.1.2-2")
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
            javaHome = file(extra["java8Home"])
            serverOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
            agentOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006")
        }
    }
}

fun Project.teamcity(configuration: TeamCityPluginExtension.() -> Unit) {
    configure(configuration)
}

fun TeamCityPluginExtension.server(configuration: ServerPluginConfiguration.() -> Unit) {
    this.server(closureOf<ServerPluginConfiguration>(configuration))
}

fun TeamCityPluginExtension.environments(configuration: TeamCityEnvironments.() -> Unit) {
    this.environments(closureOf<TeamCityEnvironments>(configuration))
}

fun ServerPluginConfiguration.descriptor(configuration: ServerPluginDescriptor.() -> Unit) {
    this.descriptor(closureOf<ServerPluginDescriptor>(configuration))
}
