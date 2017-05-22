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
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItemConsumer
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusScope
import jetbrains.buildServer.serverSide.healthStatus.ItemCategory
import jetbrains.buildServer.serverSide.healthStatus.ItemSeverity
import jetbrains.buildServer.web.openapi.PagePlace
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.junit.Test
import org.mockito.ArgumentCaptor

import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME

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

class HealthStatusTest {

    @Test
    void 'health status page is configured'() {
        PagePlaces places = mock(PagePlaces)
        PluginDescriptor descriptor = mock(PluginDescriptor)
        when(places.getPlaceById(eq(PlaceId.HEALTH_STATUS_ITEM))).thenReturn(mock(PagePlace))
        when(descriptor.getPluginResourcesPath(eq('report.jsp'))).thenReturn('pluginResourcesPath/report.jsp')

        InitScriptsHealthStatusItemExtension extension = new InitScriptsHealthStatusItemExtension(places, descriptor)

        assertThat(extension.getIncludeUrl(), equalTo('pluginResourcesPath/report.jsp'))
        assertThat(extension.getCssPaths(), hasItem('/css/admin/buildTypeForm.css'))
        assertThat(extension.isVisibleOutsideAdminArea(), is(true))
    }

    @Test
    void 'health status report configuration'() {
        GradleScriptsManager scriptsManager = mock(GradleScriptsManager)
        InitScriptsHealthStatusReport report = new InitScriptsHealthStatusReport(scriptsManager)

        assertThat(report.getType(), equalTo('gradleInitScripts'))
        assertThat(report.getDisplayName(), equalTo('Gradle Init Scripts'))
        assertThat(report.getCategories(), hasItem(new ItemCategory('gradleInitScripts', 'Gradle init scripts', ItemSeverity.WARN)))
    }

    @Test
    void 'build type with an invalid script is reported'() {
        GradleScriptsManager scriptsManager = mock(GradleScriptsManager)
        InitScriptsHealthStatusReport report = new InitScriptsHealthStatusReport(scriptsManager)

        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn([(INIT_SCRIPT_NAME): 'init.gradle'])
        SBuildType buildType = mock(SBuildType)
        when(buildType.getFullName()).thenReturn('BuildType')
        when(buildType.getBuildFeaturesOfType(FEATURE_TYPE)).thenReturn([feature])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getBuildTypes()).thenReturn([buildType])

        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        ArgumentCaptor<HealthStatusItem> itemCaptor = ArgumentCaptor.forClass(HealthStatusItem)
        verify(resultConsumer).consumeForBuildType(eq(buildType), itemCaptor.capture())

        HealthStatusItem item = itemCaptor.value
        assertThat(item.getIdentity(), equalTo('build_type_missing_init_script_BuildType'))
        assertThat(item.getSeverity(), equalTo(ItemSeverity.WARN))
        assertThat(item.getAdditionalData(), hasKey('buildType'))
        assertThat(item.getAdditionalData().get('buildType'), is(buildType))
        assertThat(item.getAdditionalData(), hasKey('errors'))
        assertThat(item.getAdditionalData().get('errors'), hasItem('init.gradle'))
    }

    @Test
    void 'build type with a valid script is not reported'() {
        GradleScriptsManager scriptsManager = mock(GradleScriptsManager)
        InitScriptsHealthStatusReport report = new InitScriptsHealthStatusReport(scriptsManager)

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
        GradleScriptsManager scriptsManager = mock(GradleScriptsManager)
        InitScriptsHealthStatusReport report = new InitScriptsHealthStatusReport(scriptsManager)

        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn([(INIT_SCRIPT_NAME): 'init.gradle'])
        BuildTypeTemplate buildTemplate = mock(BuildTypeTemplate)
        when(buildTemplate.getFullName()).thenReturn('BuildTemplate')
        when(buildTemplate.getBuildFeaturesOfType(FEATURE_TYPE)).thenReturn([feature])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getBuildTypeTemplates()).thenReturn([buildTemplate])

        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        ArgumentCaptor<HealthStatusItem> itemCaptor = ArgumentCaptor.forClass(HealthStatusItem)
        verify(resultConsumer).consumeForTemplate(eq(buildTemplate), itemCaptor.capture())

        HealthStatusItem item = itemCaptor.value
        assertThat(item.getIdentity(), equalTo('build_template_missing_init_script_BuildTemplate'))
        assertThat(item.getSeverity(), equalTo(ItemSeverity.WARN))
        assertThat(item.getAdditionalData(), hasKey('buildTemplate'))
        assertThat(item.getAdditionalData().get('buildTemplate'), is(buildTemplate))
        assertThat(item.getAdditionalData(), hasKey('errors'))
        assertThat(item.getAdditionalData().get('errors'), hasItem('init.gradle'))
    }

    @Test
    void 'build template with a valid script is not reported'() {
        GradleScriptsManager scriptsManager = mock(GradleScriptsManager)
        InitScriptsHealthStatusReport report = new InitScriptsHealthStatusReport(scriptsManager)

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
}
