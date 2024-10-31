
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
    testImplementation (group = "junit", name = "junit", version = "4.13.1")
    testImplementation (group = "org.hamcrest", name = "hamcrest-library", version = "2.2")
    testImplementation (group = "org.mockito", name = "mockito-core", version = "3.5.15")

    testRuntimeOnly (group = "org.junit.jupiter", name = "junit-jupiter-engine")
    testRuntimeOnly (group = "org.junit.vintage", name = "junit-vintage-engine")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "1.8"
    }
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
