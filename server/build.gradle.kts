
plugins {
    id ("teamcity.server-plugin")
    id ("teamcity.environments")
    id ("groovy")
}

extra["downloadsDir"] = project.findProperty("downloads.dir") ?: "${rootDir}/downloads"
extra["serversDir"] = project.findProperty("servers.dir") ?: "${rootDir}/servers"
extra["java11Home"] = project.findProperty("java11.home") ?: "/opt/jdk-11.0.2"

val teamcityVersion = project.findProperty("teamcity.api.version") as String? ?: "2018.1"

dependencies {
    implementation (project(":gradle-init-scripts-common"))
    implementation (kotlin("stdlib"))

    provided (group = "org.jetbrains.teamcity.internal", name = "server", version = teamcityVersion)

    agent (project(path = ":gradle-init-scripts-agent", configuration = "plugin"))

    testImplementation (localGroovy())
}

teamcity {
    server {
        archiveName = "${rootProject.name}-${rootProject.version}.zip"

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
            minimumBuild = "58245"
        }

        files {
            into("kotlin-dsl") {
                from("kotlin-dsl")
            }
        }

        publish {
            token = findProperty("jetbrains.token") as String?
        }
    }

    environments {
        downloadsDir = extra["downloadsDir"] as String
        baseHomeDir = extra["serversDir"] as String
        baseDataDir = "${rootDir}/data"

        register("teamcity2018.1") {
            version = "2018.1.5"
            serverOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
            agentOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006")
        }

        register("teamcity2024.03") {
            version = "2024.03.1"
            javaHome = extra["java11Home"] as String
        }
    }
}
