
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    implementation("io.github.rodm:gradle-teamcity-plugin:1.5.5")
}

kotlin {
    jvmToolchain(8)
}
