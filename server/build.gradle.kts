
plugins {
    kotlin("jvm")
    id ("groovy")
    id ("org.gradle.jacoco")
    id ("com.github.rodm.teamcity-server")
    id ("com.github.rodm.teamcity-environments")
}

extra["downloadsDir"] = project.findProperty("downloads.dir") ?: "${rootDir}/downloads"
extra["serversDir"] = project.findProperty("servers.dir") ?: "${rootDir}/servers"
extra["java8Home"] = project.findProperty("java8.home") ?: "/opt/jdk1.8.0_131"

dependencies {
    implementation (project(":common"))
    implementation (kotlin("stdlib"))

    provided (group = "org.jetbrains.teamcity.internal", name = "server", version = rootProject.extra["teamcityVersion"] as String?)

    agent (project(path = ":agent", configuration = "plugin"))

    testImplementation (localGroovy())
    testImplementation (group = "junit", name = "junit", version = "4.13.1")
    testImplementation (group = "org.hamcrest", name = "hamcrest-library", version = "2.2")
    testImplementation (group = "org.mockito", name = "mockito-core", version = "3.5.15")
}

tasks.named("test") {
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.isEnabled = true
    }
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
            minimumBuild = "58245"
        }

        publish {
            token.set(findProperty("jetbrains.token") as String?)
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

        register("teamcity2020.2") {
            version = "2020.2.1"
        }
    }
}
