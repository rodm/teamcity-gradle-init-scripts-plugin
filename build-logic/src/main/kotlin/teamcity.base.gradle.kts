
plugins {
    id ("io.github.rodm.teamcity-base")
}

teamcity {
    version = project.findProperty("teamcity.api.version") as String? ?: "2025.03"
}
