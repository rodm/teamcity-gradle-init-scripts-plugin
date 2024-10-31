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

import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_CONTENT
import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.ArgumentMatchers.matches
import org.mockito.Mockito.anyString
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import java.io.File

class GradleInitScriptsBuildFeatureTest {

    val FEATURE_PARAMETERS = hashMapOf(INIT_SCRIPT_NAME to "init.gradle", INIT_SCRIPT_CONTENT to "content")

    @TempDir
    lateinit var tempDir: File

    val runner = mock(BuildRunnerContext::class.java)

    var hasGradleInitScriptFeature = true

    val feature = object: GradleInitScriptsFeature(EventDispatcher.create(AgentLifeCycleListener::class.java)) {
        override fun hasGradleInitScriptFeature(context: BuildRunnerContext): Boolean {
            return hasGradleInitScriptFeature
        }

        override fun getBuildTempDirectory(context: BuildRunnerContext): File {
            return tempDir
        }
    }

    @BeforeEach
    fun setup() {
        `when`(runner.runnerParameters).thenReturn(FEATURE_PARAMETERS)
    }

    @Test
    fun `when feature is enabled it adds an additional --init-script to the args passed to Gradle`() {
        feature.beforeRunnerStart(runner)

        verify(runner).addRunnerParameter(eq("ui.gradleRunner.additional.gradle.cmd.params"), matches("--init-script .* "))
    }

    @Test
    fun `when feature is enabled it writes an init script to the temporary build directory`() {
        feature.beforeRunnerStart(runner)

        val files = tempDir.list()?.toList()
        assertThat(files, hasItem(startsWith("init_")))
        assertThat(files, hasItem(endsWith(".gradle")))
    }

    @Test
    fun `when feature is enabled it writes the init script content to a file`() {
        feature.beforeRunnerStart(runner)

        val files = tempDir.listFiles()
        assertThat(files?.get(0)?.readText(), equalTo("content"))
    }

    @Test
    fun `when feature is enabled it removes the init script file after the build finishes`() {
        feature.beforeRunnerStart(runner)

        val filesBeforeBuild = tempDir.list()?.toList()
        feature.runnerFinished(runner, BuildFinishedStatus.FINISHED_SUCCESS)

        val filesAfterBuild = tempDir.list()?.toList()
        assertThat(filesBeforeBuild, hasSize(1))
        assertThat(filesAfterBuild, hasSize(0))
    }

    @Test
    fun `when feature is disabled it does not add an additional --init-script argument`() {
        hasGradleInitScriptFeature = false

        feature.beforeRunnerStart(runner)

        verify(runner, never()).addRunnerParameter(anyString(), anyString())
    }
}
