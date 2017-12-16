package TeamCityPlugins_GradleInitScripts

import jetbrains.buildServer.configs.kotlin.v2017_2.version
import jetbrains.buildServer.configs.kotlin.v2017_2.project
import jetbrains.buildServer.configs.kotlin.v2017_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2017_2.CheckoutMode
import jetbrains.buildServer.configs.kotlin.v2017_2.Template
import jetbrains.buildServer.configs.kotlin.v2017_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2017_2.projectFeatures.VersionedSettings
import jetbrains.buildServer.configs.kotlin.v2017_2.projectFeatures.versionedSettings
import jetbrains.buildServer.configs.kotlin.v2017_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2017_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2017_2.vcs.GitVcsRoot

version = "2017.2"
project {
    uuid = "2e06c443-72c3-4edb-96fa-14adba937d27"
    id = "TeamCityPlugins_GradleInitScripts"
    parentId = "TeamCityPlugins"
    name = "Gradle Init Scripts"

    val vcsId = "TeamCityPlugins_GradleInitScripts_GradleInitScripts"
    val vcs = GitVcsRoot({
        uuid = "0521af81-2e0a-44ef-9004-3c7bb142d05d"
        id = vcsId
        name = "gradle-init-scripts"
        url = "https://github.com/rodm/teamcity-gradle-init-scripts-plugin.git"
        useMirrors = false
    })
    vcsRoot(vcs)

    features {
        versionedSettings {
            id = "PROJECT_EXT_1"
            mode = VersionedSettings.Mode.ENABLED
            rootExtId = vcsId
            showChanges = true
            settingsFormat = VersionedSettings.Format.KOTLIN
            buildSettingsMode = VersionedSettings.BuildSettingsMode.PREFER_SETTINGS_FROM_VCS
        }
    }

    params {
        param("env.GRADLE_OPTS", """
            -Dorg.gradle.daemon=false -Dkotlin.daemon.enabled=false -Dkotlin.compiler.execution.strategy=in-process
            -Dkotlin.colors.enabled=false
        """.trimIndent())
    }

    val buildTemplate = Template({
        uuid = "268a7880-3505-4283-b6eb-e9b6ec471150"
        id = "TeamCityPlugins_GradleInitScripts_Build"
        name = "build"

        params {
            param("gradle.opts", "")
            param("gradle.tasks", "clean build")
        }

        vcs {
            root(vcs)
            checkoutMode = CheckoutMode.ON_SERVER
        }

        steps {
            gradle {
                id = "RUNNER_1"
                tasks = "%gradle.tasks%"
                buildFile = "build.gradle.kts"
                gradleParams = "%gradle.opts%"
                useGradleWrapper = true
                enableStacktrace = true
                jdkHome = "%java8.home%"
            }
        }

        triggers {
            vcs {
                id = "vcsTrigger"
                quietPeriodMode = VcsTrigger.QuietPeriodMode.USE_DEFAULT
            }
        }

        failureConditions {
            executionTimeoutMin = 20
        }

        features {
            feature {
                id = "perfmon"
                type = "perfmon"
            }
        }
    })
    template(buildTemplate)

    buildType(BuildType({
        template(buildTemplate)
        uuid = "5f3fa0bd-698c-4494-a0a1-723b2b6dcd67"
        id = "TeamCityPlugins_GradleInitScripts_BuildTeamCity100"
        name = "Build - TeamCity 10.0"

        features {
            feature {
                id = "jvm-monitor-plugin"
                type = "jvm-monitor-plugin"
            }
        }
    }))
    buildType(BuildType({
        template(buildTemplate)
        uuid = "b7332c3b-11ba-46ba-9da4-6defe8caf4e1"
        id = "TeamCityPlugins_GradleInitScripts_ReportCodeQuality"
        name = "Report - Code Quality"

        params {
            param("gradle.opts", "%sonar.opts%")
            param("gradle.tasks", "clean build sonarqube")
        }
    }))
}
