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

package teamcity.gradle.scripts.agent

import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.util.EventDispatcher
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.endsWith
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.startsWith
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.ArgumentMatchers.matches
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.never
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class GradleInitScriptsFeatureTest extends GradleInitScriptsFeature {

    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder()

    boolean hasGradleInitScriptFeature = true

    private Map<String, String> RUNNER_PARAMETERS = ['initScriptName': 'init.gradle', 'initScriptContent': 'content']

    GradleInitScriptsFeatureTest() {
        super(EventDispatcher.create(AgentLifeCycleListener))
    }

    @Override
    boolean hasGradleInitScriptFeature(BuildRunnerContext context) {
        return hasGradleInitScriptFeature
    }

    @Override
    File getBuildTempDirectory(BuildRunnerContext context) {
        return tempDir.root
    }

    @Test
    void 'when feature is enable additional --init-script arg is passed to Gradle'() {
        BuildRunnerContext runner = mock(BuildRunnerContext)
        when(runner.getRunnerParameters()).thenReturn(RUNNER_PARAMETERS)

        beforeRunnerStart(runner)

        verify(runner).addRunnerParameter(eq('ui.gradleRunner.additional.gradle.cmd.params'), matches('--init-script .* '))
    }

    @Test
    void 'when feature is disabled no --init-script arg is passed to Gradle'() {
        BuildRunnerContext runner = mock(BuildRunnerContext)
        hasGradleInitScriptFeature = false

        beforeRunnerStart(runner)

        verify(runner, never()).addRunnerParameter(anyString(), anyString())
    }

    @Test
    void 'when feature is enabled an init script is written to the temporary build directory'() {
        BuildRunnerContext runner = mock(BuildRunnerContext)
        when(runner.getRunnerParameters()).thenReturn(RUNNER_PARAMETERS)

        beforeRunnerStart(runner)

        List<String> files = tempDir.root.list() as List
        assertThat(files, hasItem(startsWith('init_')))
        assertThat(files, hasItem(endsWith('.gradle')))
    }

    @Test
    void 'when feature is enabled after the build finishes the init script is removed'() {
        BuildRunnerContext runner = mock(BuildRunnerContext)
        when(runner.getRunnerParameters()).thenReturn(RUNNER_PARAMETERS)
        beforeRunnerStart(runner)

        List<String> filesBeforeBuild = tempDir.root.list() as List

        runnerFinished(runner, BuildFinishedStatus.FINISHED_SUCCESS)

        List<String> filesAfterBuild = tempDir.root.list() as List
        assertThat(filesBeforeBuild, hasSize(1))
        assertThat(filesAfterBuild, hasSize(0))
    }

    @Test
    void 'feature writes init script content to a file'() {
        BuildRunnerContext runner = mock(BuildRunnerContext)
        when(runner.getRunnerParameters()).thenReturn(RUNNER_PARAMETERS)
        beforeRunnerStart(runner)

        File[] files = tempDir.root.listFiles()
        assertThat(files[0].text, equalTo('content'))
    }
}
