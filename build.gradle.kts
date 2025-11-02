
plugins {
    id ("org.sonarqube") version "6.3.1.5724"
}

group = "com.github.rodm"
version = "2.0"

sonarqube {
    properties {
        property("sonar.projectKey", "${project.group}:teamcity-gradle-init-scripts")
        property("sonar.projectName", "teamcity-gradle-init-scripts")
    }
}
