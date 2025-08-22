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

import jetbrains.buildServer.controllers.ActionMessages
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.auth.AccessDeniedException
import jetbrains.buildServer.serverSide.auth.AuthorityHolder
import jetbrains.buildServer.serverSide.auth.Permission
import jetbrains.buildServer.serverSide.auth.SecurityContext
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.jdom.Element
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import static org.hamcrest.CoreMatchers.equalTo
import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.MatcherAssert.assertThat
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
    private ProjectManager projectManager = mock(ProjectManager)
    private GradleScriptsManager scriptsManager = mock(GradleScriptsManager)
    private SecurityContext securityContext = mock(SecurityContext)
    private AuthorityHolder authorityHolder = mock(AuthorityHolder)

    private HttpServletRequest request = mock(HttpServletRequest)
    private HttpServletResponse response = mock(HttpServletResponse)
    private HttpSession session = mock(HttpSession)

    private Element ajaxResponse = new Element("response")
    private ActionMessages messages = new ActionMessages()

    @BeforeEach
    void setup() {
        WebControllerManager controllerManager = mock(WebControllerManager)
        actionsController = new InitScriptsActionsController(controllerManager)
        when(authorityHolder.isPermissionGrantedForProject(anyString(), eq(Permission.EDIT_PROJECT))).thenReturn(true)
        when(securityContext.getAuthorityHolder()).thenReturn(authorityHolder)

        when(request.getSession()).thenReturn(session)
        when(request.getParameter(eq('name'))).thenReturn('init.gradle')
        when(request.getParameter(eq('projectId'))).thenReturn('projectId')
        when(request.getAttribute('actionMessages')).thenReturn(messages)
    }

    private DeleteScriptAction createDeleteAction() {
        new DeleteScriptAction(actionsController, projectManager, scriptsManager, securityContext)
    }

    @Test
    void 'action registers with action controller'() {
        InitScriptsActionsController controller = spy(actionsController)
        DeleteScriptAction action = new DeleteScriptAction(controller, projectManager, scriptsManager, securityContext)

        verify(controller).registerAction(eq(action))
    }

    @Test
    void 'can handle deleteScript action'() {
        DeleteScriptAction action = createDeleteAction()
        when(request.getParameter(eq('action'))).thenReturn('deleteScript')

        assertThat(action.canProcess(request), is(true))
    }

    @Test
    void 'deletes a script from a project'() {
        DeleteScriptAction action = createDeleteAction()
        SProject project = mock(SProject)
        when(projectManager.findProjectById('projectId')).thenReturn(project)

        action.process(request, response, ajaxResponse)

        verify(scriptsManager).deleteScript(eq(project), eq('init.gradle'))
    }

    @Test
    void 'delete action message reports file deletion is scheduled'() {
        DeleteScriptAction action = createDeleteAction()
        when(projectManager.findProjectById('projectId')).thenReturn(mock(SProject))

        action.process(request, response, ajaxResponse)

        def messages = ActionMessages.getMessages(request)
        def expectedMessage = 'Gradle init script init.gradle has been scheduled for deletion'
        assertThat(messages.getMessage('initScriptsMessage'), equalTo(expectedMessage))
    }

    @Test
    void 'delete action message reports file deletion is completed'() {
        DeleteScriptAction action = createDeleteAction()
        when(projectManager.findProjectById('projectId')).thenReturn(mock(SProject))
        when(scriptsManager.deleteScript(any(), eq('init.gradle'))).thenReturn(true)

        action.process(request, response, ajaxResponse)

        def messages = ActionMessages.getMessages(request)
        def expectedMessage = 'Gradle init script init.gradle was deleted'
        assertThat(messages.getMessage('initScriptsMessage'), equalTo(expectedMessage))
    }

    @Test
    void 'invalid project id does not delete script'() {
        DeleteScriptAction action = createDeleteAction()
        when(projectManager.findProjectById('projectId')).thenReturn(null)

        action.process(request, response, ajaxResponse)

        verify(scriptsManager, never()).deleteScript(any(SProject), anyString())
    }

    @Test
    void 'invalid project id reports error message'() {
        DeleteScriptAction action = createDeleteAction()
        when(projectManager.findProjectById('projectId')).thenReturn(null)

        action.process(request, response, ajaxResponse)

        def error = ajaxResponse.getChild('errors').children.get(0) as Element
        assertThat(error.text, equalTo('File init.gradle cannot be deleted. Project projectId was not found'))
    }

    @Test
    void 'user without edit permission reports error message'() {
        DeleteScriptAction action = createDeleteAction()
        when(projectManager.findProjectById('projectId')).thenReturn(mock(SProject))
        when(authorityHolder.isPermissionGrantedForProject(anyString(), eq(Permission.EDIT_PROJECT))).thenReturn(false)

        action.process(request, response, ajaxResponse)

        def error = ajaxResponse.getChild('errors').children.get(0) as Element
        assertThat(error.text, equalTo('You do not have permissions to edit project settings'))
    }

    @Test
    void 'exception during delete reports error message'() {
        DeleteScriptAction action = createDeleteAction()
        when(projectManager.findProjectById('projectId')).thenReturn(mock(SProject))
        when(scriptsManager.deleteScript(any(), any())).thenThrow(AccessDeniedException)

        action.process(request, response, ajaxResponse)

        def error = ajaxResponse.getChild('errors').children.get(0) as Element
        assertThat(error.text, equalTo('Gradle init script init.gradle cannot be deleted.'))
    }
}
