
plugins {
    kotlin("jvm")
}

apply {
    plugin("org.gradle.jacoco")
    plugin("com.github.rodm.teamcity-common")
}

configurations {
    all {
        exclude(module = "xom")
    }
}
