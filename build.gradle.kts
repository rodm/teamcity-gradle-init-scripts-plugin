
plugins {
    id ("org.sonarqube") version "4.0.0.2929"
}

group = "com.github.rodm"
version = "1.1-SNAPSHOT"

sonarqube {
    properties {
        property("sonar.projectKey", "${project.group}:teamcity-gradle-init-scripts")
        property("sonar.projectName", "teamcity-gradle-init-scripts")
    }
}
