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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

class ProjectInspectorTest {

    private GradleScriptsManager scriptsManager

    private ProjectInspector inspector

    @BeforeEach
    void setup() {
        scriptsManager = mock(GradleScriptsManager)
        inspector = new ProjectInspector(scriptsManager)
    }

    @Test
    void 'build type with an invalid script is reported'() {
        SProject project = mock(SProject)
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn([(INIT_SCRIPT_NAME): 'init.gradle'])
        SBuildType buildType = mock(SBuildType)
        when(buildType.getBuildFeaturesOfType(FEATURE_TYPE)).thenReturn([feature])
        when(project.getOwnBuildTypes()).thenReturn([buildType])

        List<ProjectReport> result = inspector.report(project)

        assertThat(result, hasSize(1))

        ProjectReport report = result.get(0)
        assertThat(report.project, sameInstance(project))
        assertThat(report.buildTemplates.values(), hasSize(0))
        assertThat(report.buildTypes.values(), hasSize(1))
        assertThat(report.buildTypes.get(buildType), equalTo('init.gradle'))
    }

    @Test
    void 'build type with a valid script is not reported'() {
        SProject project = mock(SProject)
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn([(INIT_SCRIPT_NAME): 'init.gradle'])
        SBuildType buildType = mock(SBuildType)
        when(buildType.getBuildFeaturesOfType(FEATURE_TYPE)).thenReturn([feature])
        when(project.getOwnBuildTypes()).thenReturn([buildType])

        when(scriptsManager.findScript(eq(project), eq('init.gradle'))).thenReturn('init script content')

        List<ProjectReport> result = inspector.report(project)

        assertThat(result, hasSize(0))
    }

    @Test
    void 'build template with an invalid script is reported'() {
        SProject project = mock(SProject)
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn([(INIT_SCRIPT_NAME): 'init.gradle'])
        BuildTypeTemplate buildTemplate = mock(BuildTypeTemplate)
        when(buildTemplate.getBuildFeaturesOfType(FEATURE_TYPE)).thenReturn([feature])
        when(project.getOwnBuildTypeTemplates()).thenReturn([buildTemplate])

        List<ProjectReport> result = inspector.report(project)

        assertThat(result, hasSize(1))

        ProjectReport report = result.get(0)
        assertThat(report.project, sameInstance(project))
        assertThat(report.buildTypes.values(), hasSize(0))
        assertThat(report.buildTemplates.values(), hasSize(1))
        assertThat(report.buildTemplates.get(buildTemplate), equalTo('init.gradle'))
    }

    @Test
    void 'build template with a valid script is not reported'() {
        SProject project = mock(SProject)
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn([(INIT_SCRIPT_NAME): 'init.gradle'])
        BuildTypeTemplate buildTemplate = mock(BuildTypeTemplate)
        when(buildTemplate.getBuildFeaturesOfType(FEATURE_TYPE)).thenReturn([feature])
        when(project.getOwnBuildTypeTemplates()).thenReturn([buildTemplate])

        when(scriptsManager.findScript(eq(project), eq('init.gradle'))).thenReturn('init script content')

        List<ProjectReport> result = inspector.report(project)

        assertThat(result, hasSize(0))
    }
}
