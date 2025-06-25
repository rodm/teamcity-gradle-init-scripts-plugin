
plugins {
    id ("org.sonarqube") version "6.2.0.5505"
}

group = "com.github.rodm"
version = "1.1-SNAPSHOT"

sonarqube {
    properties {
        property("sonar.projectKey", "${project.group}:teamcity-gradle-init-scripts")
        property("sonar.projectName", "teamcity-gradle-init-scripts")
    }
}
