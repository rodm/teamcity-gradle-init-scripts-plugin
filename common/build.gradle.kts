
apply {
    plugin("kotlin")
    plugin("org.gradle.jacoco")
    plugin("com.github.rodm.teamcity-common")
}

configurations {
    all {
        exclude(module = "xom")
    }
}
