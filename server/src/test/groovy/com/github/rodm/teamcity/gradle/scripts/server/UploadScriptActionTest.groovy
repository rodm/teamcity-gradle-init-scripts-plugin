/*
 * Copyright 2022 Rod MacKenzie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
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
import jetbrains.buildServer.serverSide.auth.AuthorityHolder
import jetbrains.buildServer.serverSide.auth.Permission
import jetbrains.buildServer.serverSide.auth.SecurityContext
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockMultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.mockito.Mockito.any
import static org.mockito.Mockito.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

class UploadScriptActionTest {

    private ProjectManager projectManager = mock(ProjectManager)
    private WebControllerManager controllerManager = mock(WebControllerManager)
    private GradleScriptsManager scriptsManager = mock(GradleScriptsManager)
    private SecurityContext securityContext = mock(SecurityContext)
    private AuthorityHolder authorityHolder = mock(AuthorityHolder)
    private SProject project = mock(SProject)

    private HttpServletRequest request = mock(MultipartHttpServletRequest)
    private HttpServletResponse response = mock(HttpServletResponse)
    private HttpSession session = mock(HttpSession)

    private ActionMessages messages = new ActionMessages()

    private UploadScriptAction action

    @BeforeEach
    void setup() {
        when(request.getSession()).thenReturn(session)
        when(request.getParameter('fileName')).thenReturn('init.gradle')
        when(request.getParameter('project')).thenReturn('projectId')
        def multipartFile = new MockMultipartFile('name', 'content'.bytes)
        when(request.getFile('file:fileToUpload')).thenReturn(multipartFile)
        when(request.getAttribute('actionMessages')).thenReturn(messages)

        when(projectManager.findProjectByExternalId('projectId')).thenReturn(project)
        when(project.getProjectId()).thenReturn('projectId')
        when(securityContext.getAuthorityHolder()).thenReturn(authorityHolder)
        when(authorityHolder.isPermissionGrantedForProject(any(), eq(Permission.EDIT_PROJECT))).thenReturn(true)

        action = new UploadScriptAction(projectManager, controllerManager, scriptsManager, securityContext)
    }

    @Test
    void 'controller registers with controller manager'() {
        verify(controllerManager).registerController(eq('/admin/uploadInitScript.html'), eq(action))
    }

    @Test
    void 'uploads a Groovy init script to a project'() {
        when(request.getParameter('fileName')).thenReturn('init-script.gradle')

        action.doPost(request, response)

        verify(scriptsManager).saveScript(eq(project), eq('init-script.gradle'), eq('content'))
    }

    @Test
    void 'uploads a Kotlin init script to a project'() {
        when(request.getParameter('fileName')).thenReturn('init-script.gradle.kts')

        action.doPost(request, response)

        verify(scriptsManager).saveScript(eq(project), eq('init-script.gradle.kts'), eq('content'))
    }

    @Test
    void 'upload action message reports file is scheduled to be saved'() {
        action.doPost(request, response)

        def messages = ActionMessages.getMessages(request)
        def expectedMessage = 'Gradle init script init.gradle has been scheduled for saving'
        assertThat(messages.getMessage('initScriptsMessage'), equalTo(expectedMessage))
    }

    @Test
    void 'upload action message reports file was uploaded'() {
        when(scriptsManager.saveScript(any(), eq('init.gradle'), any())).thenReturn(true)

        action.doPost(request, response)

        def messages = ActionMessages.getMessages(request)
        def expectedMessage = 'Gradle init script init.gradle was uploaded'
        assertThat(messages.getMessage('initScriptsMessage'), equalTo(expectedMessage))
    }

    @Test
    void 'upload action message reports file is scheduled to be updated'() {
        when(scriptsManager.findScript(any(), any())).thenReturn("content")

        action.doPost(request, response)

        def messages = ActionMessages.getMessages(request)
        def expectedMessage = 'Gradle init script init.gradle has been scheduled for updating'
        assertThat(messages.getMessage('initScriptsMessage'), equalTo(expectedMessage))
    }

    @Test
    void 'upload action message reports file was updated'() {
        when(scriptsManager.findScript(any(), any())).thenReturn("content")
        when(scriptsManager.saveScript(any(), eq('init.gradle'), any())).thenReturn(true)

        action.doPost(request, response)

        def messages = ActionMessages.getMessages(request)
        def expectedMessage = 'Gradle init script init.gradle was updated'
        assertThat(messages.getMessage('initScriptsMessage'), equalTo(expectedMessage))
    }

    @Test
    void 'empty filename returns error message '() {
        when(request.getParameter('fileName')).thenReturn('')

        ModelAndView modelAndView = action.doPost(request, response)

        assertThat(modelAndView.model['error'], equalTo('File name must be provided'))
    }

    @Test
    void 'invalid filename with parent path returns error message'() {
        when(request.getParameter('fileName')).thenReturn('../init-script.gradle')

        ModelAndView modelAndView = action.doPost(request, response)

        assertThat(modelAndView.model['error'], equalTo('Invalid file name provided for init script'))
    }

    @Test
    void 'invalid filename with subdirectory path returns error message'() {
        when(request.getParameter('fileName')).thenReturn('directory/init-script.gradle')

        ModelAndView modelAndView = action.doPost(request, response)

        assertThat(modelAndView.model['error'], equalTo('Invalid file name provided for init script'))
    }

    @Test
    void 'invalid filename extension returns error message'() {
        when(request.getParameter('fileName')).thenReturn('init-script.txt')

        ModelAndView modelAndView = action.doPost(request, response)

        assertThat(modelAndView.model['error'], equalTo('Invalid extension provided for init script'))
    }

    @Test
    void 'invalid project'() {
        when(projectManager.findProjectByExternalId(any())).thenReturn(null)

        ModelAndView modelAndView = action.doPost(request, response)

        assertThat(modelAndView.model['error'], equalTo('Cannot upload file. Project is missing'))
    }

    @Test
    void 'user without edit permission reports error message'() {
        when(authorityHolder.isPermissionGrantedForProject(any(), eq(Permission.EDIT_PROJECT))).thenReturn(false)

        ModelAndView modelAndView = action.doPost(request, response)

        assertThat(modelAndView.model['error'], equalTo('You do not have permissions to edit project settings'))
    }

    @Test
    void 'no file set'() {
        when(request.getFile('file:fileToUpload')).thenReturn(null)

        ModelAndView modelAndView = action.doPost(request, response)

        assertThat(modelAndView.model['error'], equalTo('No file set'))
    }
}
