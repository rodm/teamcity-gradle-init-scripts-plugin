
plugins {
    id ("teamcity.agent-plugin")
}

dependencies {
    implementation (project(":common"))
    implementation (kotlin("stdlib"))

    testImplementation (kotlin("reflect"))
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
