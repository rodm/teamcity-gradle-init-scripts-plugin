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

import com.github.rodm.teamcity.gradle.scripts.server.health.ProjectInspector
import jetbrains.buildServer.controllers.admin.projects.EditProjectTab
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.auth.AuthorityHolder
import jetbrains.buildServer.serverSide.auth.Permission
import jetbrains.buildServer.serverSide.auth.SecurityContext
import jetbrains.buildServer.web.openapi.PagePlace
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import javax.servlet.http.HttpServletRequest

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.hasKey
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.not
import static org.mockito.ArgumentMatchers.anyString
import static org.mockito.Mockito.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class GradleInitScriptsPageTest {

    private static final String PLUGIN_NAME = "gradleInitScripts"

    private PagePlaces places
    private PluginDescriptor descriptor
    private GradleScriptsManager scriptsManager
    private SecurityContext securityContext
    private SProject project
    private GradleInitScriptsPage page

    @BeforeEach
    void setup() {
        places = mock(PagePlaces)
        descriptor = mock(PluginDescriptor)
        scriptsManager = mock(GradleScriptsManager)
        securityContext = mock(SecurityContext)
        project = mock(SProject)

        when(places.getPlaceById(eq(PlaceId.EDIT_PROJECT_PAGE_TAB))).thenReturn(mock(PagePlace))
        when(descriptor.getPluginName()).thenReturn(PLUGIN_NAME)
        when(descriptor.getPluginResourcesPath(eq('projectPage.jsp'))).thenReturn('pluginResourcesPath/projectPage.jsp')
        when(descriptor.getPluginResourcesPath(eq('initScripts.js'))).thenReturn('pluginResourcesPath/initScripts.js')

        ProjectInspector inspector = new ProjectInspector(scriptsManager)
        page = new GradleInitScriptsPage(places, descriptor, scriptsManager, inspector, securityContext)
    }

    @Test
    void 'page is configured with required resources'() {
        assertThat(page.getTabTitle(), equalTo('Gradle Init Scripts'))
        assertThat(page.getIncludeUrl(), equalTo('pluginResourcesPath/projectPage.jsp'))
        assertThat(page.getJsPaths(), hasSize(1))
        assertThat(page.getJsPaths(), hasItem('pluginResourcesPath/initScripts.js'))
        assertThat(page.getCssPaths(), hasSize(1))
        assertThat(page.getCssPaths(), hasItem('/css/admin/buildTypeForm.css'))
    }

    @Test
    void 'page title shows scripts count'() {
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(scriptsManager.getScriptsCount(eq(project))).thenReturn(2)

        assertThat(page.getTabTitle(request), equalTo('Gradle Init Scripts (2)'))
    }

    @Test
    void 'page fills model with list of scripts'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(scriptsManager.getScriptNames(eq(project))).thenReturn([(project): ['init1.gradle', 'init2.gradle']])

        page.fillModel(model, request)

        assertThat(model, hasKey('scripts'))
        List<String> scripts = model.get('scripts') as List
        assertThat(scripts, hasItem('init1.gradle'))
        assertThat(scripts, hasItem('init2.gradle'))
    }

    @Test
    void 'model filled with file name and content when file requested'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(request.getParameter("file")).thenReturn("init.gradle")
        when(scriptsManager.findScript(eq(project), eq('init.gradle'))).thenReturn('file contents')
        when(project.getProjectId()).thenReturn("projectId")
        AuthorityHolder authorityHolder = mock(AuthorityHolder)
        when(authorityHolder.isPermissionGrantedForProject(anyString(), eq(Permission.EDIT_PROJECT))).thenReturn(true)
        when(securityContext.getAuthorityHolder()).thenReturn(authorityHolder)

        page.fillModel(model, request)

        assertThat(model, hasKey('fileName'))
        assertThat(model, hasKey('fileContent'))
        assertThat(model.get('fileName') as String, equalTo('init.gradle'))
        assertThat(model.get('fileContent') as String, equalTo('file contents'))
    }

    @Test
    void 'model not filled with file name and content when requested file does not exist'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(request.getParameter("file")).thenReturn("init.gradle")
        when(scriptsManager.findScript(eq(project), eq('init.gradle'))).thenReturn(null)
        when(project.getProjectId()).thenReturn("projectId")
        AuthorityHolder authorityHolder = mock(AuthorityHolder)
        when(authorityHolder.isPermissionGrantedForProject(anyString(), eq(Permission.EDIT_PROJECT))).thenReturn(true)
        when(securityContext.getAuthorityHolder()).thenReturn(authorityHolder)

        page.fillModel(model, request)

        assertThat(model, not(hasKey('fileName')))
        assertThat(model, not(hasKey('fileContent')))
    }

    @Test
    void 'model not filled with file name and content when file not requested'() {
        Map<String, Object> model = [:]
        HttpServletRequest request = mock(HttpServletRequest)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)

        page.fillModel(model, request)

        assertThat(model, not(hasKey('fileName')))
        assertThat(model, not(hasKey('fileContent')))
    }
}
