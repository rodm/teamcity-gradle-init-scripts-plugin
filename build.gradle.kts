
plugins {
    id ("org.sonarqube") version "7.0.1.6134"
}

group = "com.github.rodm"
version = "1.1-SNAPSHOT"

sonarqube {
    properties {
        property("sonar.projectKey", "${project.group}:teamcity-gradle-init-scripts")
        property("sonar.projectName", "teamcity-gradle-init-scripts")
    }
}
