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

import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.SRunnerContext
import jetbrains.buildServer.serverSide.SRunningBuild
import jetbrains.buildServer.serverSide.UnknownRunType
import org.junit.Before
import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.is
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.never
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_CONTENT
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME

class InitScriptsProviderTest {

    private InitScriptsProvider provider

    private GradleScriptsManager scriptsManager

    private boolean isGradleRunner = true

    @Before
    void setup() {
        scriptsManager = mock(GradleScriptsManager)
        provider = new InitScriptsProvider(scriptsManager) {
            @Override
            boolean isGradleRunner(SRunnerContext runnerContext) {
                return isGradleRunner
            }
        }
    }

    @Test
    void 'feature parameters should only be provided for build steps using the Gradle runner'() {
        SRunnerContext runnerContext = mock(SRunnerContext)
        Collection<SRunnerContext> runnerContexts = [runnerContext]

        Map<String, String> parameters = ['initScriptName': 'init.gradle']
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn(parameters)
        Collection<SBuildFeatureDescriptor> features = [feature]
        SProject project = mock(SProject)
        SBuildType buildType = mock(SBuildType)
        when(buildType.getBuildFeaturesOfType(eq(FEATURE_TYPE))).thenReturn(features)
        when(buildType.getProject()).thenReturn(project)

        SRunningBuild runningBuild = mock(SRunningBuild)
        when(runningBuild.getBuildType()).thenReturn(buildType)
        when(runningBuild.getProjectId()).thenReturn('project1')

        BuildStartContext context = mock(BuildStartContext)
        when(context.getRunnerContexts()).thenReturn(runnerContexts)
        when(context.getBuild()).thenReturn(runningBuild)

        when(scriptsManager.findScript(eq(project), eq('init.gradle'))).thenReturn('script content')

        provider.updateParameters(context)

        verify(runnerContext).addRunnerParameter(eq(INIT_SCRIPT_NAME), eq('init.gradle'))
        verify(runnerContext).addRunnerParameter(eq(INIT_SCRIPT_CONTENT), eq('script content'))
    }

    @Test
    void 'features parameters should not be provided for build steps using other build runners'() {
        isGradleRunner = false
        SRunnerContext runnerContext = mock(SRunnerContext)
        Collection<SRunnerContext> runnerContexts = [runnerContext]
        BuildStartContext context = mock(BuildStartContext)
        when(context.getRunnerContexts()).thenReturn(runnerContexts)

        provider.updateParameters(context)

        verify(runnerContext, never()).addRunnerParameter(anyString(), anyString())
    }

    @Test
    void 'when script is missing pass the script name to the agent for logging'() {
        SRunnerContext runnerContext = mock(SRunnerContext)
        Collection<SRunnerContext> runnerContexts = [runnerContext]

        Map<String, String> parameters = ['initScriptName': 'init.gradle']
        SBuildFeatureDescriptor feature = mock(SBuildFeatureDescriptor)
        when(feature.getParameters()).thenReturn(parameters)
        Collection<SBuildFeatureDescriptor> features = [feature]
        SProject project = mock(SProject)
        SBuildType buildType = mock(SBuildType)
        when(buildType.getBuildFeaturesOfType(eq(FEATURE_TYPE))).thenReturn(features)
        when(buildType.getProject()).thenReturn(project)

        SRunningBuild runningBuild = mock(SRunningBuild)
        when(runningBuild.getBuildType()).thenReturn(buildType)
        when(runningBuild.getProjectId()).thenReturn('project1')

        BuildStartContext context = mock(BuildStartContext)
        when(context.getRunnerContexts()).thenReturn(runnerContexts)
        when(context.getBuild()).thenReturn(runningBuild)

        when(scriptsManager.findScript(eq(project), eq('init.gradle'))).thenReturn(null)

        provider.updateParameters(context)

        verify(runnerContext).addRunnerParameter(eq(INIT_SCRIPT_NAME), eq('init.gradle'))
        verify(runnerContext, never()).addRunnerParameter(eq(INIT_SCRIPT_CONTENT), anyString())
    }

    @Test
    void 'isGradleRunner should return true for Gradle build runner type'() {
        SRunnerContext runnerContext = mock(SRunnerContext)
        when(runnerContext.getRunType()).thenReturn(new UnknownRunType('gradle-runner'))
        provider = new InitScriptsProvider(scriptsManager)

        assertThat(provider.isGradleRunner(runnerContext), is(true))
    }

    @Test
    void 'isGradleRunner should return false for other build runner types'() {
        SRunnerContext runnerContext = mock(SRunnerContext)
        when(runnerContext.getRunType()).thenReturn(new UnknownRunType('Maven2'))
        provider = new InitScriptsProvider(scriptsManager)

        assertThat(provider.isGradleRunner(runnerContext), is(false))
    }
}
