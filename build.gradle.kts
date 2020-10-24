
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.RecordingCopyTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.72" apply false
    id ("com.github.rodm.teamcity-server") version "1.3.1" apply true
    id ("com.jfrog.bintray") version "1.8.5"
    id ("org.sonarqube") version "3.0"
}

extra["teamcityVersion"] = project.findProperty("teamcity.api.version") as String? ?: "2018.1"

group = "com.github.rodm"
version = "1.1-SNAPSHOT"

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
