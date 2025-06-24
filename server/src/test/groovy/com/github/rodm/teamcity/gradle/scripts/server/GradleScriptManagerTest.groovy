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

import jetbrains.buildServer.serverSide.ConfigAction
import jetbrains.buildServer.serverSide.ConfigActionFactory
import jetbrains.buildServer.serverSide.CopiedObjects
import jetbrains.buildServer.serverSide.PersistTask
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.VersionedSettingsRegistry
import jetbrains.buildServer.serverSide.auth.AccessDeniedException
import jetbrains.buildServer.serverSide.impl.ProjectEx
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import java.nio.file.Files
import java.nio.file.Path

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsString
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.hasKey
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.nullValue
import static org.junit.jupiter.api.Assertions.assertThrows
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class GradleScriptManagerTest {

    private static final String PLUGIN_NAME = "gradleInitScripts"

    private GradleScriptsManager scriptsManager

    private VersionedSettingsRegistry settingsRegistry

    private ConfigActionFactory actionFactory

    @TempDir
    Path configDir

    private ProjectEx project

    @BeforeEach
    void setup() {
        PluginDescriptor descriptor = mock(PluginDescriptor)
        when(descriptor.getPluginName()).thenReturn(PLUGIN_NAME)
        settingsRegistry = mock(VersionedSettingsRegistry)
        actionFactory = mock(ConfigActionFactory)
        scriptsManager = new DefaultGradleScriptsManager(descriptor, settingsRegistry, actionFactory)
    }

    private ProjectEx createMockProject(String name, Map<String, String> scripts = [:]) {
        def project = mock(ProjectEx)
        def pluginDir = createPluginDir(configDir, name)
        when(project.getPluginDataDirectory(PLUGIN_NAME)).thenReturn(pluginDir)
        scripts.each { entry ->
            Files.write(pluginDir.toPath().resolve(entry.key), [entry.value])
        }
        return project
    }

    private static File createPluginDir(Path configDir, String name) {
        def pluginDir = configDir.resolve(name).resolve('pluginData').resolve(PLUGIN_NAME)
        Files.createDirectories(pluginDir)
        return pluginDir.toFile()
    }

    private static Answer createAnswer(ProjectEx project) {
        return new Answer() {
            Object answer(InvocationOnMock invocation) {
                def args = invocation.arguments
                def path = project.getPluginDataDirectory(PLUGIN_NAME).toPath().resolve(args[1].toString())
                Files.write(path, args[2] as byte[])
                return mock(PersistTask)
            }
        }
    }

    @Test
    void 'manager registers plugin directory with versioned settings registry'() {
        verify(settingsRegistry).registerDir(eq('pluginData/' + PLUGIN_NAME))
    }

    @Nested
    class WithEmptyProject {
        @BeforeEach
        void setup() {
            project = createMockProject('empty-project')
        }

        @Test
        void 'project with no scripts returns an empty map'() {
            Map<SProject, List<String>> scripts = scriptsManager.getScriptNames(project)
            assertThat(scripts.keySet(), hasSize(0))
        }
    }

    @Nested
    class WithSingleProject {
        @BeforeEach
        void setup() {
            project = createMockProject('project',
                ['init1.gradle': 'contents of script1', 'init2.gradle': 'contents of script2']
            )
            when(project.getProjectPath()).thenReturn([project])
        }

        @Test
        void 'project with scripts returns list of names'() {
            Map<SProject, List<String>> scripts = scriptsManager.getScriptNames(project)
            assertThat(scripts.get(project), hasSize(2))
            assertThat(scripts.get(project), hasItem('init1.gradle'))
            assertThat(scripts.get(project), hasItem('init2.gradle'))
        }

        @Test
        void 'find returns content for a script that exists'() {
            String contents = scriptsManager.findScript(project, 'init1.gradle')
            assertThat(contents.trim(), equalTo('contents of script1'))
        }

        @Test
        void 'find returns null for a script that doesn\'t exist'() {
            String contents = scriptsManager.findScript(project, 'dummy.gradle')
            assertThat(contents, is(nullValue()))
        }

        @Test
        void 'save writes a script to a project '() {
            when(project.scheduleFileSave(any(), any(), any())).then(createAnswer(project))
            when(actionFactory.createAction(any(SProject), any(String))).thenReturn(mock(ConfigAction))

            scriptsManager.saveScript(project, 'test.gradle', 'contents of test.gradle')

            assertThat(new File(project.getPluginDataDirectory(PLUGIN_NAME), 'test.gradle').exists(), is(true))
            String contents = scriptsManager.findScript(project, 'test.gradle')
            assertThat(contents, equalTo('contents of test.gradle'))

            verify(project).scheduleFileSave(any(ConfigAction), eq('test.gradle'), eq('contents of test.gradle'.bytes))
        }

        @Test
        void 'save overwrites contents of an existing script'() {
            when(project.scheduleFileSave(any(), any(), any())).then(createAnswer(project))
            when(actionFactory.createAction(any(SProject), any(String))).thenReturn(mock(ConfigAction))

            scriptsManager.saveScript(project, 'test.gradle', 'initial contents of test.gradle')
            scriptsManager.saveScript(project, 'test.gradle', 'updated contents of test.gradle')

            String contents = scriptsManager.findScript(project, 'test.gradle')
            assertThat(contents, equalTo('updated contents of test.gradle'))
        }

        @Test
        void 'delete removes a script from a project'() {
            PersistTask task = mock(PersistTask)
            when(project.scheduleFileDelete(any(), any())).thenReturn(task)
            when(task.await(any(Long))).thenReturn(true)

            assertThat(scriptsManager.deleteScript(project, 'init.gradle'), is(true))
        }

        @Test
        void 'deleting a script notifies the config file changes listener'() {
            PersistTask task = mock(PersistTask)
            when(project.scheduleFileDelete(any(), any())).thenReturn(task)
            when(actionFactory.createAction(any(SProject), any(String))).thenReturn(mock(ConfigAction))

            scriptsManager.deleteScript(project, 'test.gradle')

            verify(project).scheduleFileDelete(any(ConfigAction), eq('test.gradle'))
        }

        @Test
        void 'uploading a new script creates a config action with script uploaded message'() {
            when(project.scheduleFileSave(any(), any(), any())).then(createAnswer(project))
            when(actionFactory.createAction(any(SProject), any(String))).thenReturn(mock(ConfigAction))

            scriptsManager.saveScript(project, 'test.gradle', 'contents of test.gradle')

            verify(actionFactory).createAction(eq(project), eq('Gradle init script test.gradle was uploaded'))
        }

        @Test
        void 'updating a script creates a config action with script updated message'() {
            when(project.scheduleFileSave(any(), any(), any())).then(createAnswer(project))
            when(actionFactory.createAction(any(SProject), any(String))).thenReturn(mock(ConfigAction))

            new File(project.getPluginDataDirectory(PLUGIN_NAME), 'test.gradle') << 'initial contents of test.gradle'

            scriptsManager.saveScript(project, 'test.gradle', 'updated contents of test.gradle')

            verify(actionFactory).createAction(eq(project), eq('Gradle init script test.gradle was updated'))
        }

        @Test
        void 'deleting a script creates a config action with script deleted message'() {
            PersistTask task = mock(PersistTask)
            when(project.scheduleFileDelete(any(), any())).thenReturn(task)

            scriptsManager.deleteScript(project, 'test.gradle')

            verify(actionFactory).createAction(eq(project), eq('Gradle init script test.gradle was deleted'))
        }

        @Test
        void 'copying a project copies plugin data'() {
            ProjectEx targetProject = createMockProject('target')
            when(project.getConfigDirectory()).thenReturn(configDir.resolve('project').toFile())
            when(targetProject.getProjectPath()).thenReturn([targetProject])
            when(actionFactory.createAction(any(String))).thenReturn(mock(ConfigAction))
            CopiedObjects copiedObjects = mock(CopiedObjects)
            when(copiedObjects.getCopiedProjectsMap()).thenReturn([(project): targetProject])

            (scriptsManager as DefaultGradleScriptsManager).mapData(copiedObjects)

            verify(targetProject).scheduleFileSave(any(ConfigAction), eq('pluginData/gradleInitScripts/init1.gradle'), any())
            verify(targetProject).scheduleFileSave(any(ConfigAction), eq('pluginData/gradleInitScripts/init2.gradle'), any())
        }

        @Test
        void 'valid path for a script is within the plugin data directory'() {
            when(project.getConfigDirectory()).thenReturn(configDir.resolve('project').toFile())

            def relPath = (scriptsManager as DefaultGradleScriptsManager).getValidRelativePath(project, 'init.gradle')

            assertThat(relPath, equalTo("pluginData/${PLUGIN_NAME}/init.gradle".toString()))
        }

        @Test
        void 'invalid path for a script is outside the plugin data directory'() {
            when(project.getConfigDirectory()).thenReturn(configDir.resolve('project').toFile())

            def e = assertThrows(AccessDeniedException, {
                (scriptsManager as DefaultGradleScriptsManager).getValidRelativePath(project, '../init.gradle')
            })

            assertThat(e.message, containsString(' is outside of the allowed directory '))
        }
    }

    @Nested
    class WithParentProject {

        private SProject parentProject

        @BeforeEach
        void setup() {
            project = createMockProject('project',
                ['init1.gradle': 'contents of script1', 'init2.gradle': 'contents of script2']
            )
            parentProject = createMockProject('parent-project',
                ['parent.gradle': 'contents of parent script']
            )
            when(project.getProjectPath()).thenReturn([parentProject, project])
        }

        @Test
        void 'project with parent returns a map of projects and scripts'() {
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
            new File(project.getPluginDataDirectory(PLUGIN_NAME), 'parent.gradle') << 'contents of override script'

            Map<SProject, List<String>> scripts = scriptsManager.getScriptNames(project)
            assertThat(scripts.keySet(), hasSize(1))
            assertThat(scripts, hasKey(project))
            assertThat(scripts.get(project), hasItem('parent.gradle'))
        }

        @Test
        void 'project with parent returns list of scripts with no duplicates'() {
            new File(parentProject.getPluginDataDirectory(PLUGIN_NAME), 'init1.gradle') << 'contents of parent script1'

            Map<SProject, List<String>> scripts = scriptsManager.getScriptNames(project)
            assertThat(scripts.get(parentProject), hasSize(1))
            assertThat(scripts.get(parentProject), hasItem('parent.gradle'))
            assertThat(scripts.get(parentProject), not(hasItem('init1.gradle')))
        }

        @Test
        void 'find returns content for a script in a parent project'() {
            String contents = scriptsManager.findScript(project, 'parent.gradle')
            assertThat(contents.trim(), equalTo('contents of parent script'))
        }
    }
}
