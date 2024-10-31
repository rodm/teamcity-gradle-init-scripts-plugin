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

package com.github.rodm.teamcity.gradle.scripts.server.health

import com.github.rodm.teamcity.gradle.scripts.server.GradleScriptsManager
import jetbrains.buildServer.serverSide.BuildTypeTemplate
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItemConsumer
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusScope
import jetbrains.buildServer.serverSide.healthStatus.ItemCategory
import jetbrains.buildServer.web.openapi.PagePlace
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.PositionConstraint
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers

import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME_PARAMETER
import static jetbrains.buildServer.serverSide.healthStatus.ItemSeverity.WARN
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.hasKey
import static org.hamcrest.Matchers.is
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.never
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class MissingInitScriptsHealthReportTest {

    private GradleScriptsManager scriptsManager
    private PagePlace pagePlace
    private MissingInitScriptsHealthReport report

    @BeforeEach
    void setup() {
        scriptsManager = mock(GradleScriptsManager)
        pagePlace = mock(PagePlace)

        PagePlaces places = mock(PagePlaces)
        PluginDescriptor descriptor = mock(PluginDescriptor)
        when(places.getPlaceById(eq(PlaceId.HEALTH_STATUS_ITEM))).thenReturn(pagePlace)
        when(descriptor.getPluginResourcesPath(eq('/health/missingInitScripts.jsp'))).thenReturn('pluginResourcesPath/health/missingInitScripts.jsp')
        report = new MissingInitScriptsHealthReport(scriptsManager, places, descriptor)
    }

    @Test
    void 'health status report configuration'() {
        assertThat(report.getType(), equalTo('MissingInitScriptsReport'))
        assertThat(report.getDisplayName(), equalTo('Missing Gradle Init Scripts'))
        assertThat(report.getCategories(), hasItem(new ItemCategory('missing_init_scripts', 'Missing Gradle init scripts', WARN)))
    }

    @Test
    void 'health status report registers extension page'() {
        ArgumentCaptor<HealthStatusItemPageExtension> extensionCaptor = ArgumentCaptor.forClass(HealthStatusItemPageExtension)
        verify(pagePlace).addExtension(extensionCaptor.capture(), ArgumentMatchers.<PositionConstraint>any())

        HealthStatusItemPageExtension extension = extensionCaptor.value
        assertThat(extension.getIncludeUrl(), equalTo('pluginResourcesPath/health/missingInitScripts.jsp'))
        assertThat(extension.getCssPaths(), hasItem('/css/admin/buildTypeForm.css'))
        assertThat(extension.isVisibleOutsideAdminArea(), is(true))
    }

    @Test
    void 'build type with an invalid script is reported'() {
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn([(INIT_SCRIPT_NAME): 'init.gradle'])
        SBuildType buildType = mock(SBuildType)
        when(buildType.getProject()).thenReturn(mock(SProject))
        when(buildType.getBuildTypeId()).thenReturn('BuildTypeId')
        when(buildType.getBuildFeaturesOfType(FEATURE_TYPE)).thenReturn([feature])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getBuildTypes()).thenReturn([buildType])

        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        ArgumentCaptor<HealthStatusItem> itemCaptor = ArgumentCaptor.forClass(HealthStatusItem)
        verify(resultConsumer).consumeForBuildType(eq(buildType), itemCaptor.capture())

        HealthStatusItem item = itemCaptor.value
        assertThat(item.getIdentity(), equalTo('missing_init_scripts_BUILD_FEATURE_BuildTypeId'))
        assertThat(item.getSeverity(), equalTo(WARN))
        assertThat(item.getAdditionalData(), hasKey('buildType'))
        assertThat(item.getAdditionalData().get('buildType'), is(buildType))
        assertThat(item.getAdditionalData(), hasKey('scriptName'))
        assertThat(item.getAdditionalData().get('scriptName'), equalTo('init.gradle'))
        assertThat(item.getAdditionalData(), hasKey('statusType'))
        assertThat(item.getAdditionalData().get('statusType'), equalTo(StatusType.BUILD_FEATURE))
    }

    @Test
    void 'build type with a valid script is not reported'() {
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn([(INIT_SCRIPT_NAME): 'init.gradle'])
        SBuildType buildType = mock(SBuildType)
        when(buildType.getFullName()).thenReturn('BuildType')
        when(buildType.getBuildFeaturesOfType(FEATURE_TYPE)).thenReturn([feature])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getBuildTypes()).thenReturn([buildType])
        SProject project = mock(SProject)
        when(buildType.getProject()).thenReturn(project)
        when(scriptsManager.findScript(eq(project), eq('init.gradle'))).thenReturn('script content')

        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        verify(resultConsumer, never()).consumeForBuildType(any(SBuildType), any(HealthStatusItem))
    }

    @Test
    void 'build template with an invalid script is reported'() {
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn([(INIT_SCRIPT_NAME): 'init.gradle'])
        BuildTypeTemplate buildTemplate = mock(BuildTypeTemplate)
        when(buildTemplate.getProject()).thenReturn(mock(SProject))
        when(buildTemplate.getId()).thenReturn('BuildTemplateId')
        when(buildTemplate.getBuildFeaturesOfType(FEATURE_TYPE)).thenReturn([feature])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getBuildTypeTemplates()).thenReturn([buildTemplate])

        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        ArgumentCaptor<HealthStatusItem> itemCaptor = ArgumentCaptor.forClass(HealthStatusItem)
        verify(resultConsumer).consumeForTemplate(eq(buildTemplate), itemCaptor.capture())

        HealthStatusItem item = itemCaptor.value
        assertThat(item.getIdentity(), equalTo('missing_init_scripts_BUILD_FEATURE_BuildTemplateId'))
        assertThat(item.getSeverity(), equalTo(WARN))
        assertThat(item.getAdditionalData(), hasKey('buildTemplate'))
        assertThat(item.getAdditionalData().get('buildTemplate'), is(buildTemplate))
        assertThat(item.getAdditionalData(), hasKey('scriptName'))
        assertThat(item.getAdditionalData().get('scriptName'), equalTo('init.gradle'))
        assertThat(item.getAdditionalData(), hasKey('statusType'))
        assertThat(item.getAdditionalData().get('statusType'), equalTo(StatusType.BUILD_FEATURE))
    }

    @Test
    void 'build template with a valid script is not reported'() {
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn([(INIT_SCRIPT_NAME): 'init.gradle'])
        BuildTypeTemplate buildTemplate = mock(BuildTypeTemplate)
        when(buildTemplate.getFullName()).thenReturn('BuildType')
        when(buildTemplate.getBuildFeaturesOfType(FEATURE_TYPE)).thenReturn([feature])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getBuildTypeTemplates()).thenReturn([buildTemplate])
        SProject project = mock(SProject)
        when(buildTemplate.getProject()).thenReturn(project)
        when(scriptsManager.findScript(eq(project), eq('init.gradle'))).thenReturn('script content')

        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        verify(resultConsumer, never()).consumeForTemplate(any(BuildTypeTemplate), any(HealthStatusItem))
    }

    @Test
    void 'build type runner with an invalid script is reported'() {
        SBuildRunnerDescriptor runner = mock(SBuildRunnerDescriptor)
        when(runner.getParameters()).thenReturn([(INIT_SCRIPT_NAME_PARAMETER): 'init.gradle'])
        SBuildType buildType = mock(SBuildType)
        when(buildType.getProject()).thenReturn(mock(SProject))
        when(buildType.getBuildTypeId()).thenReturn('BuildTypeId')
        when(buildType.getBuildRunners()).thenReturn([runner])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getBuildTypes()).thenReturn([buildType])

        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        ArgumentCaptor<HealthStatusItem> itemCaptor = ArgumentCaptor.forClass(HealthStatusItem)
        verify(resultConsumer).consumeForBuildType(eq(buildType), itemCaptor.capture())

        HealthStatusItem item = itemCaptor.value
        assertThat(item.getIdentity(), equalTo('missing_init_scripts_BUILD_RUNNER_BuildTypeId'))
        assertThat(item.getSeverity(), equalTo(WARN))
        assertThat(item.getAdditionalData(), hasKey('buildType'))
        assertThat(item.getAdditionalData().get('buildType'), is(buildType))
        assertThat(item.getAdditionalData(), hasKey('scriptName'))
        assertThat(item.getAdditionalData().get('scriptName'), equalTo('init.gradle'))
        assertThat(item.getAdditionalData(), hasKey('statusType'))
        assertThat(item.getAdditionalData().get('statusType'), equalTo(StatusType.BUILD_RUNNER))
    }

    @Test
    void 'build type runner with a valid script is not reported'() {
        SBuildRunnerDescriptor runner = mock(SBuildRunnerDescriptor)
        when(runner.getParameters()).thenReturn([(INIT_SCRIPT_NAME_PARAMETER): 'init.gradle'])
        SBuildType buildType = mock(SBuildType)
        when(buildType.getFullName()).thenReturn('BuildType')
        when(buildType.getBuildRunners()).thenReturn([runner])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getBuildTypes()).thenReturn([buildType])
        SProject project = mock(SProject)
        when(buildType.getProject()).thenReturn(project)
        when(scriptsManager.findScript(eq(project), eq('init.gradle'))).thenReturn('script content')

        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        verify(resultConsumer, never()).consumeForBuildType(any(SBuildType), any(HealthStatusItem))
    }

    @Test
    void 'build template runner with an invalid script is reported'() {
        SBuildRunnerDescriptor runner = mock(SBuildRunnerDescriptor)
        when(runner.getParameters()).thenReturn([(INIT_SCRIPT_NAME_PARAMETER): 'init.gradle'])
        BuildTypeTemplate buildTemplate = mock(BuildTypeTemplate)
        when(buildTemplate.getProject()).thenReturn(mock(SProject))
        when(buildTemplate.getId()).thenReturn('BuildTemplateId')
        when(buildTemplate.getBuildRunners()).thenReturn([runner])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getBuildTypeTemplates()).thenReturn([buildTemplate])

        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        ArgumentCaptor<HealthStatusItem> itemCaptor = ArgumentCaptor.forClass(HealthStatusItem)
        verify(resultConsumer).consumeForTemplate(eq(buildTemplate), itemCaptor.capture())

        HealthStatusItem item = itemCaptor.value
        assertThat(item.getIdentity(), equalTo('missing_init_scripts_BUILD_RUNNER_BuildTemplateId'))
        assertThat(item.getSeverity(), equalTo(WARN))
        assertThat(item.getAdditionalData(), hasKey('buildTemplate'))
        assertThat(item.getAdditionalData().get('buildTemplate'), is(buildTemplate))
        assertThat(item.getAdditionalData(), hasKey('scriptName'))
        assertThat(item.getAdditionalData().get('scriptName'), equalTo('init.gradle'))
        assertThat(item.getAdditionalData(), hasKey('statusType'))
        assertThat(item.getAdditionalData().get('statusType'), equalTo(StatusType.BUILD_RUNNER))
    }

    @Test
    void 'build template runner with a valid script is not reported'() {
        SBuildRunnerDescriptor runner = mock(SBuildRunnerDescriptor)
        when(runner.getParameters()).thenReturn([(INIT_SCRIPT_NAME_PARAMETER): 'init.gradle'])
        BuildTypeTemplate buildTemplate = mock(BuildTypeTemplate)
        when(buildTemplate.getFullName()).thenReturn('BuildType')
        when(buildTemplate.getBuildRunners()).thenReturn([runner])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getBuildTypeTemplates()).thenReturn([buildTemplate])
        SProject project = mock(SProject)
        when(buildTemplate.getProject()).thenReturn(project)
        when(scriptsManager.findScript(eq(project), eq('init.gradle'))).thenReturn('script content')

        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        verify(resultConsumer, never()).consumeForTemplate(any(BuildTypeTemplate), any(HealthStatusItem))
    }
}
