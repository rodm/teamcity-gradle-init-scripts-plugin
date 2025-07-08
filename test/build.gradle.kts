
plugins {
    id ("teamcity.environments")
}

val plugins by configurations.creating

dependencies {
    plugins (project(path = ":gradle-init-scripts-server", configuration = "plugin"))
}

teamcity {
    environments {
        register("teamcity2024.12") {
            version = "2024.12"
            plugins = configurations["plugins"]
            serverOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005")
            agentOptions ("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5006")
        }
    }
}
