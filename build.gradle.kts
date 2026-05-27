
plugins {
    id ("org.sonarqube") version "7.3.0.8198"
}

group = "com.github.rodm"
version = "2.1-SNAPSHOT"

sonarqube {
    properties {
        property("sonar.projectKey", "${project.group}:teamcity-gradle-init-scripts")
        property("sonar.projectName", "teamcity-gradle-init-scripts")
    }
}
