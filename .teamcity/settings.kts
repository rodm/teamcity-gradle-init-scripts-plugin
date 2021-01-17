
import jetbrains.buildServer.configs.kotlin.v2019_2.version
import jetbrains.buildServer.configs.kotlin.v2019_2.project
import jetbrains.buildServer.configs.kotlin.v2019_2.CheckoutMode.ON_SERVER
import jetbrains.buildServer.configs.kotlin.v2019_2.buildSteps.gradle
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.VcsTrigger.QuietPeriodMode.USE_DEFAULT
import jetbrains.buildServer.configs.kotlin.v2019_2.triggers.vcs
import jetbrains.buildServer.configs.kotlin.v2019_2.vcs.GitVcsRoot

version = "2020.1"

project {

    val vcsId = "GradleInitScripts"
    val vcs = GitVcsRoot {
        id(vcsId)
        name = "gradle-init-scripts"
        url = "https://github.com/rodm/teamcity-gradle-init-scripts-plugin.git"
        branchSpec = """
            +:refs/heads/(master)
            +:refs/tags/(*)
        """.trimIndent()
        useTagsAsBranches = true
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
            checkoutMode = ON_SERVER
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
                quietPeriodMode = USE_DEFAULT
                branchFilter = "+:*"
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
        id("BuildTeamCity1")
        name = "Build - TeamCity 2018.1"

        features {
            feature {
                id = "jvm-monitor-plugin"
                type = "jvm-monitor-plugin"
            }
        }
    }

    buildType {
        templates(buildTemplate)
        id("BuildTeamCity2")
        name = "Build - TeamCity 2020.2"

        params {
            param("gradle.opts", "-Pteamcity.api.version=2020.2")
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
