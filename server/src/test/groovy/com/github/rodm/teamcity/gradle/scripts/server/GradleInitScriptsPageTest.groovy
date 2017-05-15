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

import jetbrains.buildServer.controllers.admin.projects.EditProjectTab
import jetbrains.buildServer.serverSide.BuildTypeTemplate
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.web.openapi.PagePlace
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.hasKey
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.isA
import static org.hamcrest.Matchers.not
import static org.mockito.Mockito.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class GradleInitScriptsPageTest {

    private PagePlaces places

    private PluginDescriptor descriptor

    private GradleScriptsManager scriptsManager

    private SProject project

    private GradleInitScriptsPage page

    @Before
    void setup() {
        places = mock(PagePlaces)
        descriptor = mock(PluginDescriptor)
        scriptsManager = mock(GradleScriptsManager)
        project = mock(SProject)

        when(places.getPlaceById(eq(PlaceId.EDIT_PROJECT_PAGE_TAB))).thenReturn(mock(PagePlace))
        when(descriptor.getPluginResourcesPath(eq('projectPage.jsp'))).thenReturn('pluginResourcesPath/projectPage.jsp')
        when(descriptor.getPluginResourcesPath(eq('initScripts.js'))).thenReturn('pluginResourcesPath/initScripts.js')

        page = new GradleInitScriptsPage(places, descriptor, scriptsManager)
    }

    @Test
    void 'page is configured with required resources'() {
        assertThat(page.getTabTitle(), equalTo('Gradle Init Scripts'))
        assertThat(page.getIncludeUrl(), equalTo('pluginResourcesPath/projectPage.jsp'))
        assertThat(page.getJsPaths(), hasSize(1))
        assertThat(page.getJsPaths(), hasItem('pluginResourcesPath/initScripts.js'))
        assertThat(page.getCssPaths(), hasSize(1))
        assertThat(page.getCssPaths(), hasItem('/css/admin/buildTypeForm.css'))
    }

    @Test
    void 'page title shows scripts count'() {
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(scriptsManager.getScriptsCount(eq(project))).thenReturn(2)

        assertThat(page.getTabTitle(request), equalTo('Gradle Init Scripts (2)'))
    }

    @Test
    void 'page fills model with list of scripts'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(scriptsManager.getScriptNames(eq(project))).thenReturn([(project): ['init1.gradle', 'init2.gradle']])

        page.fillModel(model, request)

        assertThat(model, hasKey('scripts'))
        Map<SProject, List<String>> scripts = model.get('scripts') as Map
        assertThat(scripts.get(project), hasItem('init1.gradle'))
        assertThat(scripts.get(project), hasItem('init2.gradle'))
    }

    @Test
    void 'model filled with file name and content when file requested'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(request.getParameter("file")).thenReturn("init.gradle")
        when(scriptsManager.findScript(eq(project), eq('init.gradle'))).thenReturn('file contents')

        page.fillModel(model, request)

        assertThat(model, hasKey('fileName'))
        assertThat(model, hasKey('fileContent'))
        assertThat(model.get('fileName') as String, equalTo('init.gradle'))
        assertThat(model.get('fileContent') as String, equalTo('file contents'))
    }

    @Test
    void 'model not filled with file name and content when requested file does not exist'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(request.getParameter("file")).thenReturn("init.gradle")
        when(scriptsManager.findScript(eq(project), eq('init.gradle'))).thenReturn(null)

        page.fillModel(model, request)

        assertThat(model, not(hasKey('fileName')))
        assertThat(model, not(hasKey('fileContent')))
    }

    @Test
    void 'model not filled with file name and content when file not requested'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)

        page.fillModel(model, request)

        assertThat(model, not(hasKey('fileName')))
        assertThat(model, not(hasKey('fileContent')))
    }

    @Test
    void 'model filled with empty usage map'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)

        page.fillModel(model, request)

        assertThat(model, hasKey('usage'))
        Map<String, List<SBuildType>> usage = model.get('usage') as Map
        assertThat(usage.entrySet(), hasSize(0))
    }

    @Test
    void 'usage map has an entry for each script'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(scriptsManager.getScriptNames(eq(project))).thenReturn([(project): ['init1.gradle', 'init2.gradle']])

        page.fillModel(model, request)

        Map<String, ScriptUsage> usage = model.get('usage') as Map
        assertThat(usage, hasKey('init1.gradle'))
        assertThat(usage.get('init1.gradle'), isA(ScriptUsage))
        assertThat(usage, hasKey('init2.gradle'))
        assertThat(usage.get('init2.gradle'), isA(ScriptUsage))
    }

    @Test
    void 'usage map has a list of build types using a script'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(scriptsManager.getScriptNames(eq(project))).thenReturn([(project): ['init1.gradle', 'init2.gradle']])
        Map<String, String> parameters = ['initScriptName': 'init1.gradle']
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn(parameters)
        Collection<SBuildFeatureDescriptor> features = [feature]
        SBuildType buildType = mock(SBuildType)
        when(buildType.getBuildFeaturesOfType(eq(FEATURE_TYPE))).thenReturn(features)
        when(project.getOwnBuildTypes()).thenReturn([buildType])

        page.fillModel(model, request)

        Map<String, ScriptUsage> usage = model.get('usage') as Map
        ScriptUsage scriptUsage = usage.get('init1.gradle')
        assertThat(scriptUsage.buildTypes, hasSize(1))
        assertThat(scriptUsage.buildTypes.get(0), equalTo(buildType))
    }

    @Test
    void 'usage map has a list of build templates using a script'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(scriptsManager.getScriptNames(eq(project))).thenReturn([(project): ['init1.gradle', 'init2.gradle']])
        Map<String, String> parameters = ['initScriptName': 'init1.gradle']
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn(parameters)
        Collection<SBuildFeatureDescriptor> features = [feature]
        BuildTypeTemplate buildTemplate = mock(BuildTypeTemplate)
        when(buildTemplate.getBuildFeaturesOfType(eq(FEATURE_TYPE))).thenReturn(features)
        when(project.getOwnBuildTypeTemplates()).thenReturn([buildTemplate])

        page.fillModel(model, request)

        Map<String, ScriptUsage> usage = model.get('usage') as Map
        ScriptUsage scriptUsage = usage.get('init1.gradle')
        assertThat(scriptUsage.buildTemplates, hasSize(1))
        assertThat(scriptUsage.buildTemplates.get(0), equalTo(buildTemplate))
    }
}
