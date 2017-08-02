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

import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.junit.Before
import org.junit.Test
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when
import static org.mockito.Mockito.verify

class InitScriptsAdminControllerTest {

    private ProjectManager projectManager

    private WebControllerManager controllerManager

    private GradleScriptsManager scriptsManager

    private InitScriptsAdminController controller

    @Before
    void setup() {
        SBuildServer server = mock(SBuildServer)
        projectManager = mock(ProjectManager)
        PluginDescriptor pluginDescriptor = mock(PluginDescriptor)
        controllerManager = mock(WebControllerManager)
        scriptsManager = mock(GradleScriptsManager)
        controller = new InitScriptsAdminController(server, projectManager, pluginDescriptor, controllerManager, scriptsManager)
    }

    @Test
    void 'controller registers with controller manager'() {
        verify(controllerManager).registerController(eq('/admin/initScripts.html'), eq(controller))
    }

    @Test
    void 'missing script property is not set when selected script exists'() {
        HttpServletRequest request = mock(HttpServletRequest)
        HttpServletResponse response = mock(HttpServletResponse)
        SProject project = mock(SProject)
        when(request.getAttribute(eq('selectedScript'))).thenReturn('init.gradle')
        when(request.getParameter('projectId')).thenReturn('project1')
        when(projectManager.findProjectById('project1')).thenReturn(project)
        when(scriptsManager.findScript(eq(project), eq('init.gradle'))).thenReturn('content')

        ModelAndView modelAndView = controller.doHandle(request, response)

        assertThat(modelAndView.model['missingScript'], equalTo(null))
    }

    @Test
    void 'missing script property contains script name when selected script does not exist'() {
        HttpServletRequest request = mock(HttpServletRequest)
        HttpServletResponse response = mock(HttpServletResponse)
        SProject project = mock(SProject)
        when(request.getAttribute(eq('selectedScript'))).thenReturn('init.gradle')
        when(request.getParameter('projectId')).thenReturn('project1')
        when(projectManager.findProjectById('project1')).thenReturn(project)
        when(scriptsManager.findScript(eq(project), eq('init.gradle'))).thenReturn(null)

        ModelAndView modelAndView = controller.doHandle(request, response)

        assertThat(modelAndView.model['missingScript'] as String, equalTo('init.gradle'))
    }
}
