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
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.CoreMatchers.is
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.never
import static org.mockito.Mockito.spy
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class DeleteScriptActionTest {

    private InitScriptsActionsController actionsController

    private ProjectManager projectManager

    private GradleScriptsManager scriptsManager

    @Before
    void setup() {
        WebControllerManager controllerManager = mock(WebControllerManager)
        actionsController = new InitScriptsActionsController(controllerManager)
        projectManager = mock(ProjectManager)
        scriptsManager = mock(GradleScriptsManager)
    }

    @Test
    void 'action registers with action controller'() {
        InitScriptsActionsController controller = spy(actionsController)
        DeleteScriptAction action = new DeleteScriptAction(controller, projectManager, scriptsManager)

        verify(controller).registerAction(eq(action))
    }

    @Test
    void 'can handle deleteScript action'() {
        DeleteScriptAction action = new DeleteScriptAction(actionsController, projectManager, scriptsManager)

        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getParameter(eq('action'))).thenReturn('deleteScript')

        assertThat(action.canProcess(request), is(true))
    }

    @Test
    void 'deletes a script from a project'() {
        DeleteScriptAction action = new DeleteScriptAction(actionsController, projectManager, scriptsManager)

        HttpServletRequest request = mock(HttpServletRequest)
        HttpServletResponse response = mock(HttpServletResponse)
        HttpSession session = mock(HttpSession)

        when(request.getSession()).thenReturn(session)
        when(request.getParameter(eq('name'))).thenReturn('init.gradle')
        when(request.getParameter(eq('projectId'))).thenReturn('project1')
        SProject project = mock(SProject)
        when(projectManager.findProjectById('project1')).thenReturn(project)

        action.process(request, response, null)

        verify(scriptsManager).deleteScript(eq(project), eq('init.gradle'))
    }

    @Test
    void 'does not delete a script for an invalid project id'() {
        DeleteScriptAction action = new DeleteScriptAction(actionsController, projectManager, scriptsManager)

        HttpServletRequest request = mock(HttpServletRequest)
        HttpServletResponse response = mock(HttpServletResponse)

        when(request.getParameter(eq('name'))).thenReturn('init.gradle')
        when(request.getParameter(eq('projectId'))).thenReturn('project1')
        when(projectManager.findProjectById('project1')).thenReturn(null)

        action.process(request, response, null)

        verify(scriptsManager, never()).deleteScript(any(SProject), anyString())
    }
}
