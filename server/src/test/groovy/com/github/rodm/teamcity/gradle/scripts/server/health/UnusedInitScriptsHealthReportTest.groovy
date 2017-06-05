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

import com.github.rodm.teamcity.gradle.scripts.server.InitScriptsUsageAnalyzer
import com.github.rodm.teamcity.gradle.scripts.server.ScriptUsage
import jetbrains.buildServer.serverSide.BuildTypeTemplate
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItemConsumer
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusScope
import jetbrains.buildServer.serverSide.healthStatus.ItemCategory
import jetbrains.buildServer.web.openapi.*
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers

import static jetbrains.buildServer.serverSide.healthStatus.ItemSeverity.INFO
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

class UnusedInitScriptsHealthReportTest {

    private InitScriptsUsageAnalyzer analyzer
    private PagePlace pagePlace
    private UnusedInitScriptsHealthReport report

    @Before
    void setup() {
        analyzer = mock(InitScriptsUsageAnalyzer)
        pagePlace = mock(PagePlace)

        PagePlaces places = mock(PagePlaces)
        PluginDescriptor descriptor = mock(PluginDescriptor)
        when(places.getPlaceById(eq(PlaceId.HEALTH_STATUS_ITEM))).thenReturn(pagePlace)
        when(descriptor.getPluginResourcesPath(eq('/health/unusedInitScripts.jsp'))).thenReturn('pluginResourcesPath/health/unusedInitScripts.jsp')
        report = new UnusedInitScriptsHealthReport(places, descriptor, analyzer)
    }

    @Test
    void 'health status report configuration'() {
        assertThat(report.getType(), equalTo('UnusedInitScriptsReport'))
        assertThat(report.getDisplayName(), equalTo('Unused Gradle init scripts'))
        assertThat(report.getCategories(), hasItem(new ItemCategory('unused_init_scripts', 'Unused Gradle init scripts', INFO)))
    }

    @Test
    void 'health status report registers extension page'() {
        ArgumentCaptor<HealthStatusItemPageExtension> extensionCaptor = ArgumentCaptor.forClass(HealthStatusItemPageExtension)
        verify(pagePlace).addExtension(extensionCaptor.capture(), ArgumentMatchers.<PositionConstraint>any())

        HealthStatusItemPageExtension extension = extensionCaptor.value
        assertThat(extension.getIncludeUrl(), equalTo('pluginResourcesPath/health/unusedInitScripts.jsp'))
        assertThat(extension.isVisibleOutsideAdminArea(), is(true))
    }

    @Test
    void 'unused init script is reported'() {
        SProject project = mock(SProject)
        when(project.getProjectId()).thenReturn('projectId')
        ScriptUsage usage = new ScriptUsage()
        when(analyzer.getProjectScriptsUsage(eq(project))).thenReturn(['init.gradle': usage])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getProjects()).thenReturn([project])

        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        ArgumentCaptor<HealthStatusItem> itemCaptor = ArgumentCaptor.forClass(HealthStatusItem)
        verify(resultConsumer).consumeForProject(eq(project), itemCaptor.capture())

        HealthStatusItem item = itemCaptor.value
        assertThat(item.getIdentity(), equalTo('unused_init_scripts_projectId_init.gradle'))
        assertThat(item.getSeverity(), equalTo(INFO))
        assertThat(item.getAdditionalData(), hasKey('project'))
        assertThat(item.getAdditionalData().get('project'), is(project))
        assertThat(item.getAdditionalData(), hasKey('scriptName'))
        assertThat(item.getAdditionalData().get('scriptName'), equalTo('init.gradle'))
    }

    @Test
    void 'init script used by a build type is not reported'() {
        SProject project = mock(SProject)
        when(project.getProjectId()).thenReturn('projectId')
        ScriptUsage usage = new ScriptUsage()
        when(analyzer.getProjectScriptsUsage(eq(project))).thenReturn(['init.gradle': usage])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getProjects()).thenReturn([project])

        usage.buildTypes.add(mock(SBuildType))
        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        verify(resultConsumer, never()).consumeForProject(any(SProject), any(HealthStatusItem))
    }

    @Test
    void 'init script used by a build template is not reported'() {
        SProject project = mock(SProject)
        when(project.getProjectId()).thenReturn('projectId')
        ScriptUsage usage = new ScriptUsage()
        when(analyzer.getProjectScriptsUsage(eq(project))).thenReturn(['init.gradle': usage])
        HealthStatusScope scope = mock(HealthStatusScope)
        when(scope.getProjects()).thenReturn([project])

        usage.buildTemplates.add(mock(BuildTypeTemplate))
        HealthStatusItemConsumer resultConsumer = mock(HealthStatusItemConsumer)
        report.report(scope, resultConsumer)

        verify(resultConsumer, never()).consumeForProject(any(SProject), any(HealthStatusItem))
    }
}
