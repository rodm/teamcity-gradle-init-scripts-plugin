
plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.24")
    implementation("io.github.rodm:gradle-teamcity-plugin:1.5.5")
}
