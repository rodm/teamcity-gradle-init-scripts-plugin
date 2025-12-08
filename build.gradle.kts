
plugins {
    id ("org.sonarqube") version "7.2.0.6526"
}

group = "com.github.rodm"
version = "2.1-SNAPSHOT"

sonarqube {
    properties {
        property("sonar.projectKey", "${project.group}:teamcity-gradle-init-scripts")
        property("sonar.projectName", "teamcity-gradle-init-scripts")
    }
}
