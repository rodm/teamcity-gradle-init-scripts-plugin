
import com.github.rodm.teamcity.TeamCityPluginExtension
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.RecordingCopyTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.2.0" apply false
    id ("com.github.rodm.teamcity-server") version "1.1-beta-1" apply true
    id ("com.jfrog.bintray") version "1.8.0"
    id ("org.sonarqube") version "2.6.1"
}

extra["teamcityVersion"] = project.findProperty("teamcity.api.version") as String? ?: "10.0"

group = "com.github.rodm"
version = "0.9-SNAPSHOT"

subprojects {
    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "1.8"
        }
    }
}

teamcity {
    version = extra["teamcityVersion"] as String
}

bintray {
    user = findProperty("bintray.user") as String?
    key = findProperty("bintray.key") as String?

    filesSpec(closureOf<RecordingCopyTask> {
        from ("${project(":server").buildDir}/distributions")
        into ("gradle-init-scripts")
        include ("*.zip")
    })

    dryRun = false
    publish = true
    override = false

    pkg(closureOf<BintrayExtension.PackageConfig> {
        repo = "teamcity-plugins-generic"
        name = "gradle-init-scripts"
        desc = "A TeamCity plugin that provides support for reusing Gradle init scripts"
        websiteUrl = "https://github.com/rodm/teamcity-gradle-init-scripts-plugin"
        issueTrackerUrl = "https://github.com/rodm/teamcity-gradle-init-scripts-plugin/issues"
        vcsUrl = "https://github.com/rodm/teamcity-gradle-init-scripts-plugin.git"
        setLicenses("Apache-2.0")
        setLabels("teamcity", "plugin", "gradle", "scripts")

        version(closureOf<BintrayExtension.VersionConfig> {
            name = project.version as String
        })
    })
}

fun Project.teamcity(configuration: TeamCityPluginExtension.() -> Unit) {
    configure(configuration)
}

fun Project.bintray(configuration: BintrayExtension.() -> Unit) {
    configure(configuration)
}
