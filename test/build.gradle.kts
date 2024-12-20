
plugins {
    id ("teamcity.environments")
}

extra["downloadsDir"] = project.findProperty("downloads.dir") ?: "${rootDir}/downloads"
extra["serversDir"] = project.findProperty("servers.dir") ?: "${rootDir}/servers"
extra["java11Home"] = project.findProperty("java11.home") ?: "/opt/jdk-11.0.2"

val plugins by configurations.creating

dependencies {
    plugins (project(path = ":gradle-init-scripts-server", configuration = "plugin"))
}

teamcity {
    environments {
        downloadsDir = extra["downloadsDir"] as String
        baseHomeDir = extra["serversDir"] as String
        baseDataDir = "${rootDir}/data"

        register("teamcity2024.12") {
            version = "2024.12"
            javaHome = extra["java11Home"] as String
            plugins = configurations["plugins"]
            serverOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
            agentOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006")
        }
    }
}
