
plugins {
    kotlin("jvm")
    id ("org.gradle.jacoco")
    id ("com.github.rodm.teamcity-agent")
}

dependencies {
    compile (project(":common"))
    compile (kotlin("stdlib"))
    runtime (kotlin("runtime"))

    testCompile (kotlin("reflect"))
    testCompile (group = "junit", name = "junit", version = "4.12")
    testCompile (group = "org.hamcrest", name = "hamcrest-library", version = "1.3")
    testCompile (group = "org.mockito", name = "mockito-core", version = "2.7.22")
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
