
plugins {
    kotlin("jvm")
    id("org.gradle.jacoco")
}

dependencies {
    testImplementation (platform("org.junit:junit-bom:6.0.0"))
    testImplementation ("org.junit.jupiter:junit-jupiter-api")
    testImplementation ("org.hamcrest:hamcrest-library:3.0")
    testImplementation ("org.mockito:mockito-core:5.20.0")

    testRuntimeOnly ("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine")
}

kotlin {
    jvmToolchain(17)
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy (jacocoTestReport)
    }

    jacocoTestReport {
        reports {
            xml.required = true
        }
    }
}
