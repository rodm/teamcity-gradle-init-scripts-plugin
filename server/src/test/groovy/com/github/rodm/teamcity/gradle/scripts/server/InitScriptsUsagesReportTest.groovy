/*
 * Copyright 2024 Rod MacKenzie.
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
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor
import jetbrains.buildServer.serverSide.auth.SecurityContext
import jetbrains.buildServer.web.openapi.PagePlace
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME_PARAMETER
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasKey
import static org.hamcrest.Matchers.hasSize
import static org.mockito.Mockito.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class InitScriptsUsagesReportTest {

    private static final String PLUGIN_NAME = "gradleInitScripts"

    private PagePlaces places
    private PluginDescriptor descriptor
    private GradleScriptsManager scriptsManager
    private SecurityContext securityContext
    private SProject project
    private InitScriptsUsagesReport page

    @Before
    void setup() {
        places = mock(PagePlaces)
        descriptor = mock(PluginDescriptor)
        scriptsManager = mock(GradleScriptsManager)
        securityContext = mock(SecurityContext)
        project = mock(SProject)

        when(places.getPlaceById(eq(PlaceId.ADMIN_USAGES_FRAGMENT))).thenReturn(mock(PagePlace))
        when(descriptor.getPluginName()).thenReturn(PLUGIN_NAME)
        when(descriptor.getPluginResourcesPath(eq('usagesReport.jsp'))).thenReturn('pluginResourcesPath/usagesReport.jsp')

        InitScriptsUsageAnalyzer analyzer = new InitScriptsUsageAnalyzer(scriptsManager)
        page = new InitScriptsUsagesReport(places, descriptor, analyzer)
    }

    @Test
    void 'page is configured with required resources'() {
        assertThat(page.getIncludeUrl(), equalTo('pluginResourcesPath/usagesReport.jsp'))
    }

    @Test
    void 'page fills model with usage properties'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getParameter('scriptName')).thenReturn('init.gradle')
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)

        page.fillModel(model, request)

        assertThat(model, hasKey('currentProject'))
        assertThat(model, hasKey('scriptName'))
        assertThat(model, hasKey('buildTypeUsages'))
        assertThat(model, hasKey('templateUsages'))
    }

    @Test
    void 'page fills model with empty usage list'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getParameter('scriptName')).thenReturn('init.gradle')
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)

        page.fillModel(model, request)

        assertThat(model['scriptName'], equalTo('init.gradle'))
        assertThat(model['buildTypeUsages'], hasSize(0))
        assertThat(model['templateUsages'], hasSize(0))
    }

    @Test
    void 'build types usage has a list of build types using a script when configured as a build feature'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getParameter('scriptName')).thenReturn('init1.gradle')
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

        def buildTypeUsages = model.get('buildTypeUsages') as List<SBuildType>
        assertThat(buildTypeUsages, hasSize(1))
        assertThat(buildTypeUsages.first(), equalTo(buildType))
    }

    @Test
    void 'build types usage has a list of build types using a script when configured on a build runner'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getParameter('scriptName')).thenReturn('init1.gradle')

        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(scriptsManager.getScriptNames(eq(project))).thenReturn([(project): ['init1.gradle', 'init2.gradle']])
        Map<String, String> parameters = [(INIT_SCRIPT_NAME_PARAMETER): 'init1.gradle']
        SBuildRunnerDescriptor runner = mock(SBuildRunnerDescriptor)
        when(runner.getParameters()).thenReturn(parameters)
        List<SBuildRunnerDescriptor> runners = [runner]
        SBuildType buildType = mock(SBuildType)
        when(buildType.getBuildRunners()).thenReturn(runners)
        when(project.getOwnBuildTypes()).thenReturn([buildType])

        page.fillModel(model, request)

        def buildTypeUsages = model.get('buildTypeUsages') as List<SBuildType>
        assertThat(buildTypeUsages, hasSize(1))
        assertThat(buildTypeUsages.first(), equalTo(buildType))
    }

    @Test
    void 'templates usage has a list of build templates using a script when configured as a build feature'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getParameter('scriptName')).thenReturn('init1.gradle')
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

        def templateUsages = model.get('templateUsages') as List<BuildTypeTemplate>
        assertThat(templateUsages, hasSize(1))
        assertThat(templateUsages.first(), equalTo(buildTemplate))
    }

    @Test
    void 'templates usage has a list of build templates using a script when configured on a build runner'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getParameter('scriptName')).thenReturn('init1.gradle')
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(scriptsManager.getScriptNames(eq(project))).thenReturn([(project): ['init1.gradle', 'init2.gradle']])
        Map<String, String> parameters = [(INIT_SCRIPT_NAME_PARAMETER): 'init1.gradle']
        SBuildRunnerDescriptor runner = mock(SBuildRunnerDescriptor)
        when(runner.getParameters()).thenReturn(parameters)
        List<SBuildRunnerDescriptor> runners = [runner]
        BuildTypeTemplate buildTemplate = mock(BuildTypeTemplate)
        when(buildTemplate.getBuildRunners()).thenReturn(runners)
        when(project.getOwnBuildTypeTemplates()).thenReturn([buildTemplate])

        page.fillModel(model, request)

        def templateUsages = model.get('templateUsages') as List<BuildTypeTemplate>
        assertThat(templateUsages, hasSize(1))
        assertThat(templateUsages.first(), equalTo(buildTemplate))
    }

    @Test
    void 'build types usage should not include duplicate build types'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getParameter('scriptName')).thenReturn('init1.gradle')
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(scriptsManager.getScriptNames(eq(project))).thenReturn([(project): ['init1.gradle', 'init2.gradle']])
        SBuildRunnerDescriptor runner = mock(SBuildRunnerDescriptor)
        when(runner.getParameters()).thenReturn([(INIT_SCRIPT_NAME_PARAMETER): 'init1.gradle'])
        List<SBuildRunnerDescriptor> runners = [runner]
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn(['initScriptName': 'init1.gradle'])
        Collection<SBuildFeatureDescriptor> features = [feature]
        SBuildType buildType = mock(SBuildType)
        when(buildType.getBuildRunners()).thenReturn(runners)
        when(buildType.getBuildFeaturesOfType(eq(FEATURE_TYPE))).thenReturn(features)
        when(project.getOwnBuildTypes()).thenReturn([buildType])

        page.fillModel(model, request)

        def buildTypesUsage = model.get('buildTypeUsages') as List<SBuildType>
        assertThat(buildTypesUsage, hasSize(1))
        assertThat(buildTypesUsage.first(), equalTo(buildType))
    }

    @Test
    void 'templates usage should not include duplicate build templates'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getParameter('scriptName')).thenReturn('init1.gradle')
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(scriptsManager.getScriptNames(eq(project))).thenReturn([(project): ['init1.gradle', 'init2.gradle']])
        SBuildRunnerDescriptor runner = mock(SBuildRunnerDescriptor)
        when(runner.getParameters()).thenReturn([(INIT_SCRIPT_NAME_PARAMETER): 'init1.gradle'])
        List<SBuildRunnerDescriptor> runners = [runner]
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn(['initScriptName': 'init1.gradle'])
        Collection<SBuildFeatureDescriptor> features = [feature]
        BuildTypeTemplate buildTemplate = mock(BuildTypeTemplate)
        when(buildTemplate.getBuildRunners()).thenReturn(runners)
        when(buildTemplate.getBuildFeaturesOfType(eq(FEATURE_TYPE))).thenReturn(features)
        when(project.getOwnBuildTypeTemplates()).thenReturn([buildTemplate])

        page.fillModel(model, request)

        def templateUsages = model.get('templateUsages') as List<BuildTypeTemplate>
        assertThat(templateUsages, hasSize(1))
        assertThat(templateUsages.first(), equalTo(buildTemplate))
    }
}
