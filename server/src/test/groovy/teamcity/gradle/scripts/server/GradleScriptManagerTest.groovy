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

package teamcity.gradle.scripts.server

import jetbrains.buildServer.serverSide.SProject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.nullValue
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when
import static teamcity.gradle.scripts.common.GradleInitScriptsPlugin.PLUGIN_NAME

class GradleScriptManagerTest {

    @Rule
    public TemporaryFolder projectDir = new TemporaryFolder()

    private GradleScriptsManager scriptsManager

    private File pluginDir

    @Before
    void setup() {
        scriptsManager = new DefaultGradleScriptsManager()
        pluginDir = projectDir.newFolder(PLUGIN_NAME)
        new File(pluginDir, 'init1.gradle') << 'contents of script1'
        new File(pluginDir, 'init2.gradle') << 'contents of script2'
    }

    @Test
    void 'project with no scripts returns empty list'() {
        File emptyPluginDir = projectDir.newFolder('emptyPluginDir')
        SProject project = mock(SProject)
        when(project.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(emptyPluginDir)

        List<String> scripts = scriptsManager.getScriptNames(project)
        assertThat(scripts, hasSize(0))
    }

    @Test
    void 'project with scripts returns list of names'() {
        SProject project = mock(SProject)
        when(project.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(pluginDir)

        List<String> scripts = scriptsManager.getScriptNames(project)
        assertThat(scripts, hasSize(2))
        assertThat(scripts, hasItem('init1.gradle'))
        assertThat(scripts, hasItem('init2.gradle'))
    }

    @Test
    void 'find returns content for a script that exists'() {
        SProject project = mock(SProject)
        when(project.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(pluginDir)

        String contents = scriptsManager.findScript(project, 'init1.gradle')
        assertThat(contents, equalTo('contents of script1'))
    }

    @Test
    void 'find returns null for a script that doesn\'t exist'() {
        SProject project = mock(SProject)
        when(project.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(pluginDir)

        String contents = scriptsManager.findScript(project, 'dummy.gradle')
        assertThat(contents, is(nullValue()))
    }

    @Test
    void 'delete removes a script from a project'() {
        SProject project = mock(SProject)
        when(project.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(pluginDir)

        assertThat(scriptsManager.deleteScript(project, 'init2.gradle'), is(true))
        assertThat(new File(pluginDir, 'init2.gradle').exists(), is(false))
    }
}
