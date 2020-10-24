
plugins {
    kotlin("jvm")
    id ("org.gradle.jacoco")
    id ("com.github.rodm.teamcity-agent")
}

dependencies {
    implementation (project(":common"))
    implementation (kotlin("stdlib"))

    testImplementation (kotlin("reflect"))
    testImplementation (group = "junit", name = "junit", version = "4.13.1")
    testImplementation (group = "org.hamcrest", name = "hamcrest-library", version = "2.2")
    testImplementation (group = "org.mockito", name = "mockito-core", version = "3.5.15")
}

tasks.named("test") {
    finalizedBy (tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.isEnabled = true
    }
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
