
plugins {
    kotlin("jvm")
    id("org.gradle.jacoco")
}

val mockito = configurations.create("mockito")

dependencies {
    testImplementation (platform("org.junit:junit-bom:6.1.3"))
    testImplementation ("org.junit.jupiter:junit-jupiter-api")
    testImplementation ("org.hamcrest:hamcrest-library:3.0")
    testImplementation ("org.mockito:mockito-core:5.24.0")

    testRuntimeOnly ("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine")

    mockito ("org.mockito:mockito-core:5.24.0") { isTransitive = false }
}

kotlin {
    jvmToolchain(21)
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy (jacocoTestReport)
        jvmArgs ("-javaagent:${mockito.asPath}")
    }

    jacocoTestReport {
        reports {
            xml.required = true
        }
    }
}
