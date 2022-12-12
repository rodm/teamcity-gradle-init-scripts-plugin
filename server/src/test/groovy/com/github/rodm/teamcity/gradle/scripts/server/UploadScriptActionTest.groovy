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

import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.junit.Before
import org.junit.Test
import org.springframework.web.servlet.ModelAndView

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

class UploadScriptActionTest {

    private ProjectManager projectManager
    private WebControllerManager controllerManager
    private GradleScriptsManager scriptsManager
    private UploadScriptAction controller

    @Before
    void setup() {
        projectManager = mock(ProjectManager)
        controllerManager = mock(WebControllerManager)
        scriptsManager = mock(GradleScriptsManager)
        controller = new UploadScriptAction(projectManager, controllerManager, scriptsManager)
    }

    @Test
    void 'controller registers with controller manager'() {
        verify(controllerManager).registerController(eq('/admin/uploadInitScript.html'), eq(controller))
    }

    @Test
    void 'empty filename returns error message '() {
        HttpServletRequest request = mock(HttpServletRequest)
        HttpServletResponse response = mock(HttpServletResponse)
        when(request.getParameter('fileName')).thenReturn('')

        ModelAndView modelAndView = controller.doPost(request, response)

        assertThat(modelAndView.model['error'], equalTo('File name must be provided'))
    }

    @Test
    void 'invalid filename with parent path returns error message'() {
        HttpServletRequest request = mock(HttpServletRequest)
        HttpServletResponse response = mock(HttpServletResponse)
        when(request.getParameter('fileName')).thenReturn('../init-script.gradle')

        ModelAndView modelAndView = controller.doPost(request, response)

        assertThat(modelAndView.model['error'], equalTo('Invalid file name provided for init script'))
    }

    @Test
    void 'invalid filename with subdirectory path returns error message'() {
        HttpServletRequest request = mock(HttpServletRequest)
        HttpServletResponse response = mock(HttpServletResponse)
        when(request.getParameter('fileName')).thenReturn('directory/init-script.gradle')

        ModelAndView modelAndView = controller.doPost(request, response)

        assertThat(modelAndView.model['error'], equalTo('Invalid file name provided for init script'))
    }

    @Test
    void 'invalid filename extension returns error message'() {
        HttpServletRequest request = mock(HttpServletRequest)
        HttpServletResponse response = mock(HttpServletResponse)
        when(request.getParameter('fileName')).thenReturn('init-script.txt')

        ModelAndView modelAndView = controller.doPost(request, response)

        assertThat(modelAndView.model['error'], equalTo('Invalid extension provided for init script'))
    }

    @Test
    void 'valid filename accepted for Groovy DSL'() {
        HttpServletRequest request = mock(HttpServletRequest)
        HttpServletResponse response = mock(HttpServletResponse)
        when(request.getParameter('fileName')).thenReturn('init-script.gradle')

        ModelAndView modelAndView = controller.doPost(request, response)

        // validation of project parameter comes after filename validation
        assertThat(modelAndView.model['error'], equalTo('Cannot upload file. Project is missing'))
    }

    @Test
    void 'valid filename accepted for Kotlin DSL'() {
        HttpServletRequest request = mock(HttpServletRequest)
        HttpServletResponse response = mock(HttpServletResponse)
        when(request.getParameter('fileName')).thenReturn('init-script.gradle.kts')

        ModelAndView modelAndView = controller.doPost(request, response)

        // validation of project parameter comes after filename validation
        assertThat(modelAndView.model['error'], equalTo('Cannot upload file. Project is missing'))
    }
}
