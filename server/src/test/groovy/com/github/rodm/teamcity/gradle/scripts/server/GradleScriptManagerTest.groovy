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

package com.github.rodm.teamcity.gradle.scripts.server

import jetbrains.buildServer.serverSide.SProject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.hasKey
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.nullValue
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.PLUGIN_NAME

class GradleScriptManagerTest {

    private GradleScriptsManager scriptsManager

    @Rule
    public TemporaryFolder projectDir = new TemporaryFolder()

    private File pluginDir

    @Rule
    public TemporaryFolder parentProjectDir = new TemporaryFolder()

    private File parentPluginDir

    @Before
    void setup() {
        scriptsManager = new DefaultGradleScriptsManager()
        pluginDir = projectDir.newFolder(PLUGIN_NAME)
        new File(pluginDir, 'init1.gradle') << 'contents of script1'
        new File(pluginDir, 'init2.gradle') << 'contents of script2'
        parentPluginDir = parentProjectDir.newFolder(PLUGIN_NAME)
        new File(parentPluginDir, 'parent.gradle') << 'contents of parent script'
    }

    @Test
    void 'project with no scripts returns an empty map'() {
        File emptyPluginDir = projectDir.newFolder('emptyPluginDir')
        SProject project = mock(SProject)
        when(project.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(emptyPluginDir)

        Map<SProject, List<String>> scripts = scriptsManager.getScriptNames(project)
        assertThat(scripts.keySet(), hasSize(0))
    }

    @Test
    void 'project with scripts returns list of names'() {
        SProject project = mock(SProject)
        when(project.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(pluginDir)
        when(project.getProjectPath()).thenReturn([project])

        Map<SProject, List<String>> scripts = scriptsManager.getScriptNames(project)
        assertThat(scripts.get(project), hasSize(2))
        assertThat(scripts.get(project), hasItem('init1.gradle'))
        assertThat(scripts.get(project), hasItem('init2.gradle'))
    }

    @Test
    void 'project with parent returns a map of projects and scripts'() {
        SProject parentProject = mock(SProject)
        when(parentProject.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(parentPluginDir)
        SProject project = mock(SProject)
        when(project.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(pluginDir)
        when(project.getProjectPath()).thenReturn([parentProject, project])

        Map<SProject, List<String>> scripts = scriptsManager.getScriptNames(project)
        assertThat(scripts.keySet(), hasSize(2))
        assertThat(scripts, hasKey(project))
        assertThat(scripts, hasKey(parentProject))
        assertThat(scripts.get(project), hasSize(2))
        assertThat(scripts.get(project), hasItem('init1.gradle'))
        assertThat(scripts.get(project), hasItem('init2.gradle'))
        assertThat(scripts.get(parentProject), hasSize(1))
        assertThat(scripts.get(parentProject), hasItem('parent.gradle'))
    }

    @Test
    void 'parent project should not be returned if subproject overrides all the parent scripts'() {
        SProject parentProject = mock(SProject)
        when(parentProject.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(parentPluginDir)
        SProject project = mock(SProject)
        when(project.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(pluginDir)
        when(project.getProjectPath()).thenReturn([parentProject, project])
        new File(pluginDir, 'parent.gradle') << 'contents of override script'

        Map<SProject, List<String>> scripts = scriptsManager.getScriptNames(project)
        assertThat(scripts.keySet(), hasSize(1))
        assertThat(scripts, hasKey(project))
        assertThat(scripts.get(project), hasItem('parent.gradle'))
    }

    @Test
    void 'project with parent returns list of scripts with no duplicates'() {
        SProject parentProject = mock(SProject)
        when(parentProject.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(parentPluginDir)
        SProject project = mock(SProject)
        when(project.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(pluginDir)
        when(project.getProjectPath()).thenReturn([parentProject, project])
        new File(parentPluginDir, 'init1.gradle') << 'contents of parent script1'

        Map<SProject, List<String>> scripts = scriptsManager.getScriptNames(project)
        assertThat(scripts.get(parentProject), hasSize(1))
        assertThat(scripts.get(parentProject), hasItem('parent.gradle'))
        assertThat(scripts.get(parentProject), not(hasItem('init1.gradle')))
    }

    @Test
    void 'find returns content for a script that exists'() {
        SProject project = mock(SProject)
        when(project.getProjectPath()).thenReturn([project])
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
    void 'find returns content for a script in a parent project'() {
        SProject parentProject = mock(SProject)
        when(parentProject.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(parentPluginDir)
        SProject project = mock(SProject)
        when(project.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(pluginDir)
        when(project.getProjectPath()).thenReturn([parentProject, project])

        String contents = scriptsManager.findScript(project, 'parent.gradle')
        assertThat(contents, equalTo('contents of parent script'))
    }

    @Test
    void 'delete removes a script from a project'() {
        SProject project = mock(SProject)
        when(project.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(pluginDir)

        assertThat(scriptsManager.deleteScript(project, 'init2.gradle'), is(true))
        assertThat(new File(pluginDir, 'init2.gradle').exists(), is(false))
    }
}
