/*
 * Copyright 2017 Rod MacKenzie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.rodm.teamcity.gradle.scripts.agent

import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME
import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_CONTENT
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.util.EventDispatcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.endsWith
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasSize
import org.hamcrest.Matchers.startsWith
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.junit.rules.TemporaryFolder
import org.mockito.ArgumentMatchers.matches
import org.mockito.Mockito.*
import java.io.File

class GradleInitScriptsFeatureTest : Spek({

    val RUNNER_PARAMETERS = hashMapOf(INIT_SCRIPT_NAME to "init.gradle", INIT_SCRIPT_CONTENT to "content")

    describe("a Gradle Init Script feature") {
        var hasGradleInitScriptFeature = true
        val tempDir = TemporaryFolder()
        beforeEachTest {
            tempDir.create()
        }
        afterEachTest {
            tempDir.delete()
        }
        val feature = object: GradleInitScriptsFeature(EventDispatcher.create(AgentLifeCycleListener::class.java)) {
            override fun hasGradleInitScriptFeature(context: BuildRunnerContext): Boolean {
                return hasGradleInitScriptFeature
            }

            override fun getBuildTempDirectory(context: BuildRunnerContext): File {
                return tempDir.root
            }
        }

        on("being enabled for a build") {
            val runner = mock(BuildRunnerContext::class.java)
            `when`(runner.runnerParameters).thenReturn(RUNNER_PARAMETERS)

            feature.beforeRunnerStart(runner)

            it("adds an additional --init-script to the args passed to Gradle") {
                verify(runner).addRunnerParameter(eq("ui.gradleRunner.additional.gradle.cmd.params"), matches("--init-script .* "))
            }

            it("writes an init script to the temporary build directory") {
                val files = tempDir.root.list().toList()
                assertThat(files, hasItem(startsWith("init_")))
                assertThat(files, hasItem(endsWith(".gradle")))
            }

            it("writes the init script content to a file") {
                val files = tempDir.root.listFiles()
                assertThat(files[0].readText(), equalTo("content"))
            }

            it("removes the init script file after the build finishes") {
                val filesBeforeBuild = tempDir.root.list().toList()

                feature.runnerFinished(runner, BuildFinishedStatus.FINISHED_SUCCESS)

                val filesAfterBuild = tempDir.root.list().toList()
                assertThat(filesBeforeBuild, hasSize(1))
                assertThat(filesAfterBuild, hasSize(0))

            }
        }

        on("being disabled for a build") {
            hasGradleInitScriptFeature = false
            val runner = mock(BuildRunnerContext::class.java)
            `when`(runner.runnerParameters).thenReturn(RUNNER_PARAMETERS)

            feature.beforeRunnerStart(runner)

            it("does not add an additional --init-script arg") {
                verify(runner, never()).addRunnerParameter(anyString(), anyString())
            }
        }
    }
})
