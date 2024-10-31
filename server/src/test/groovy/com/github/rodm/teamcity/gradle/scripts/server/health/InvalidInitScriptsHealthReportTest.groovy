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
import jetbrains.buildServer.serverSide.BuildTypeSettings
import jetbrains.buildServer.serverSide.BuildTypeTemplate
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItemConsumer
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusScope
import jetbrains.buildServer.serverSide.healthStatus.ItemCategory
import jetbrains.buildServer.web.openapi.*
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers

import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME
import static jetbrains.buildServer.serverSide.healthStatus.ItemSeverity.INFO
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

class InvalidInitScriptsHealthReportTest {

    private GradleScriptsManager scriptsManager
    private PagePlace pagePlace
    private InvalidInitScriptsHealthReport report
    private boolean hasGradleBuildRunner = false

    @BeforeEach
    void setup() {
        scriptsManager = mock(GradleScriptsManager)
        pagePlace = mock(PagePlace)

        PagePlaces places = mock(PagePlaces)
        PluginDescriptor descriptor = mock(PluginDescriptor)
        when(places.getPlaceById(eq(PlaceId.HEALTH_STATUS_ITEM))).thenReturn(pagePlace)
        when(descriptor.getPluginResourcesPath(eq('/health/invalidInitScripts.jsp'))).thenReturn('pluginResourcesPath/health/invalidInitScripts.jsp')
        report = new InvalidInitScriptsHealthReport(places, descriptor) {
            @Override
            boolean hasGradleRunnerBuildStep(BuildTypeSettings buildTypeSettings) {
                return hasGradleBuildRunner
            }
        }
    }

    @Test
    void 'health status report configuration'() {
        assertThat(report.getType(), equalTo('InvalidInitScriptsReport'))
        assertThat(report.getDisplayName(), equalTo('Invalid Gradle Init Scripts configuration'))
        assertThat(report.getCategories(), hasItem(new ItemCategory('invalid_init_scripts', 'Invalid Gradle init scripts configuration', INFO)))
    }

    @Test
    void 'health status report registers extension page'() {
        ArgumentCaptor<HealthStatusItemPageExtension> extensionCaptor = ArgumentCaptor.forClass(HealthStatusItemPageExtension)
        verify(pagePlace).addExtension(extensionCaptor.capture(), ArgumentMatchers.<PositionConstraint>any())

        HealthStatusItemPageExtension extension = extensionCaptor.value
        assertThat(extension.getIncludeUrl(), equalTo('pluginResourcesPath/health/invalidInitScripts.jsp'))
        assertThat(extension.getCssPaths(), hasItem('/css/admin/buildTypeForm.css'))
        assertThat(extension.isVisibleOutsideAdminArea(), is(true))
    }

    @Test
    void 'build type without Gradle build runner is reported'() {
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn([(INIT_SCRIPT_NAME): 'init.gradle'])
        SBuildType buildType = mock(SBuildType)
        when(buildType.getBuildTypeId()).thenReturn('BuildTypeId')
        when(buildType.getBuildFeaturesOfType(FEATURE_TYPE)).thenReturn([feature])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getBuildTypes()).thenReturn([buildType])

        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        ArgumentCaptor<HealthStatusItem> itemCaptor = ArgumentCaptor.forClass(HealthStatusItem)
        verify(resultConsumer).consumeForBuildType(eq(buildType), itemCaptor.capture())

        HealthStatusItem item = itemCaptor.value
        assertThat(item.getIdentity(), equalTo('invalid_init_scripts_BuildTypeId'))
        assertThat(item.getSeverity(), equalTo(INFO))
        assertThat(item.getAdditionalData(), hasKey('buildType'))
        assertThat(item.getAdditionalData().get('buildType'), is(buildType))
        assertThat(item.getAdditionalData(), hasKey('scriptName'))
        assertThat(item.getAdditionalData().get('scriptName'), equalTo('init.gradle'))
    }

    @Test
    void 'build type with Gradle build runner is not reported'() {
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn([(INIT_SCRIPT_NAME): 'init.gradle'])
        SBuildType buildType = mock(SBuildType)
        when(buildType.getBuildTypeId()).thenReturn('BuildTypeId')
        when(buildType.getBuildFeaturesOfType(FEATURE_TYPE)).thenReturn([feature])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getBuildTypes()).thenReturn([buildType])

        hasGradleBuildRunner = true
        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        verify(resultConsumer, never()).consumeForBuildType(any(SBuildType), any(HealthStatusItem))
    }

    @Test
    void 'build template without Gradle build runner is reported'() {
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn([(INIT_SCRIPT_NAME): 'init.gradle'])
        BuildTypeTemplate buildTemplate = mock(BuildTypeTemplate)
        when(buildTemplate.getId()).thenReturn('BuildTemplateId')
        when(buildTemplate.getBuildFeaturesOfType(FEATURE_TYPE)).thenReturn([feature])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getBuildTypeTemplates()).thenReturn([buildTemplate])

        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        ArgumentCaptor<HealthStatusItem> itemCaptor = ArgumentCaptor.forClass(HealthStatusItem)
        verify(resultConsumer).consumeForTemplate(eq(buildTemplate), itemCaptor.capture())

        HealthStatusItem item = itemCaptor.value
        assertThat(item.getIdentity(), equalTo('invalid_init_scripts_BuildTemplateId'))
        assertThat(item.getSeverity(), equalTo(INFO))
        assertThat(item.getAdditionalData(), hasKey('buildTemplate'))
        assertThat(item.getAdditionalData().get('buildTemplate'), is(buildTemplate))
        assertThat(item.getAdditionalData(), hasKey('scriptName'))
        assertThat(item.getAdditionalData().get('scriptName'), equalTo('init.gradle'))
    }

    @Test
    void 'build template with Gradle build runner is not reported'() {
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn([(INIT_SCRIPT_NAME): 'init.gradle'])
        BuildTypeTemplate buildTemplate = mock(BuildTypeTemplate)
        when(buildTemplate.getTemplateId()).thenReturn('BuildTemplateId')
        when(buildTemplate.getBuildFeaturesOfType(FEATURE_TYPE)).thenReturn([feature])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getBuildTypeTemplates()).thenReturn([buildTemplate])

        hasGradleBuildRunner = true
        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        verify(resultConsumer, never()).consumeForTemplate(any(BuildTypeTemplate), any(HealthStatusItem))
    }
}
