
plugins {
    id ("teamcity.server-plugin")
    id ("groovy")
}

dependencies {
    implementation (project(":gradle-init-scripts-common"))
    implementation (kotlin("stdlib"))

    provided ("org.jetbrains.teamcity.internal:server:${teamcity.version}")

    agent (project(path = ":gradle-init-scripts-agent", configuration = "plugin"))

    testImplementation (localGroovy())
    testImplementation ("org.jetbrains.teamcity.internal:server:${teamcity.version}")
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
            minimumBuild = "186049"
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
}
