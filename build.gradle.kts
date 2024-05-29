
plugins {
    id ("teamcity.base")
    id ("org.sonarqube") version "4.0.0.2929"
}

extra["teamcityVersion"] = project.findProperty("teamcity.api.version") as String? ?: "2018.1"

group = "com.github.rodm"
version = "1.1-SNAPSHOT"

teamcity {
    version = extra["teamcityVersion"] as String
}
