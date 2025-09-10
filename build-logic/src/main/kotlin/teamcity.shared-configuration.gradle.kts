
plugins {
    kotlin("jvm")
    id("org.gradle.jacoco")
}

dependencies {
    testImplementation (platform("org.junit:junit-bom:5.11.3"))
    testImplementation ("org.junit.jupiter:junit-jupiter-api")
    testImplementation ("org.hamcrest:hamcrest-library:3.0")
    testImplementation ("org.mockito:mockito-core:5.18.0")

    testRuntimeOnly ("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly ("org.junit.jupiter:junit-jupiter-engine")
}

kotlin {
    jvmToolchain(17)
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    finalizedBy (tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    reports {
        xml.required.set(true)
    }
}
