
plugins {
    kotlin("jvm")
    id("org.gradle.jacoco")
}

configurations.all {
    exclude(group = "com.ibm.icu", module = "icu4j")
}

dependencies {
    testImplementation (platform("org.junit:junit-bom:5.11.3"))
    testImplementation (group = "org.junit.jupiter", name = "junit-jupiter-api")
    testImplementation (group = "org.hamcrest", name = "hamcrest-library", version = "3.0")
    testImplementation (group = "org.mockito", name = "mockito-core", version = "5.18.0")

    testRuntimeOnly (group = "org.junit.platform", name = "junit-platform-launcher")
    testRuntimeOnly (group = "org.junit.jupiter", name = "junit-jupiter-engine")
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
