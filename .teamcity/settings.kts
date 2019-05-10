
import jetbrains.buildServer.configs.kotlin.v2018_2.version
import jetbrains.buildServer.configs.kotlin.v2018_2.project
import jetbrains.buildServer.configs.kotlin.v2018_2.CheckoutMode
import jetbrains.buildServer.configs.kotlin.v2018_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.VcsTrigger
import jetbrains.buildServer.configs.kotlin.v2018_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2018_2.vcs.GitVcsRoot

version = "2018.2"

project {

    val vcsId = "GradleInitScripts"
    val vcs = GitVcsRoot {
        id(vcsId)
        name = "gradle-init-scripts"
        url = "https://github.com/rodm/teamcity-gradle-init-scripts-plugin.git"
        useMirrors = false
    }
    vcsRoot(vcs)

    params {
        param("teamcity.ui.settings.readOnly", "true")

        param("env.GRADLE_OPTS", """
            -Dorg.gradle.daemon=false -Dkotlin.daemon.enabled=false -Dkotlin.compiler.execution.strategy=in-process
            -Dkotlin.colors.enabled=false
        """.trimIndent())
    }

    val buildTemplate = template {
        id("Build")
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
    }

    buildType {
        templates(buildTemplate)
        id("BuildTeamCity100")
        name = "Build - TeamCity 10.0"

        features {
            feature {
                id = "jvm-monitor-plugin"
                type = "jvm-monitor-plugin"
            }
        }
    }

    buildType {
        templates(buildTemplate)
        id("BuildTeamCity20171")
        name = "Build - TeamCity 2017.1"

        params {
            param("gradle.opts", "-Pteamcity.api.version=2017.1")
        }
    }

    buildType {
        templates(buildTemplate)
        id("BuildTeamCity20172")
        name = "Build - TeamCity 2017.2"

        params {
            param("gradle.opts", "-Pteamcity.api.version=2017.2")
        }
    }

    buildType {
        templates(buildTemplate)
        id("BuildTeamCity20181")
        name = "Build - TeamCity 2018.1"

        params {
            param("gradle.opts", "-Pteamcity.api.version=2018.1")
        }
    }

    buildType {
        templates(buildTemplate)
        id("BuildTeamCity20182")
        name = "Build - TeamCity 2018.2"

        params {
            param("gradle.opts", "-Pteamcity.api.version=2018.2")
        }
    }

    buildType {
        templates(buildTemplate)
        id("ReportCodeQuality")
        name = "Report - Code Quality"

        params {
            param("gradle.opts", "%sonar.opts%")
            param("gradle.tasks", "clean build sonarqube")
        }
    }
}
