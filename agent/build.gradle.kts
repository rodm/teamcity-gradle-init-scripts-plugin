
plugins {
    id ("teamcity.agent-plugin")
}

dependencies {
    implementation (project(":gradle-init-scripts-common"))
    implementation (kotlin("stdlib"))

    testImplementation (kotlin("reflect"))
}

teamcity {
    agent {
        descriptor {
            pluginDeployment {
                useSeparateClassloader = true
            }
        }
    }
}
