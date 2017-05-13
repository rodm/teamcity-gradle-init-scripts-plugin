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

import jetbrains.buildServer.controllers.admin.projects.EditProjectTab
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.web.openapi.PagePlace
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PlaceId
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.junit.Before
import org.junit.Test

import javax.servlet.http.HttpServletRequest

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.hasItem
import static org.hamcrest.Matchers.hasKey
import static org.hamcrest.Matchers.hasSize
import static org.mockito.Mockito.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class GradleInitScriptsPageTest {

    private PagePlaces places

    private PluginDescriptor descriptor

    private GradleScriptsManager scriptsManager

    @Before
    void setup() {
        places = mock(PagePlaces)
        descriptor = mock(PluginDescriptor)
        scriptsManager = mock(GradleScriptsManager)

        when(places.getPlaceById(eq(PlaceId.EDIT_PROJECT_PAGE_TAB))).thenReturn(mock(PagePlace))
        when(descriptor.getPluginResourcesPath(eq('projectPage.jsp'))).thenReturn('pluginResourcesPath/projectPage.jsp')
        when(descriptor.getPluginResourcesPath(eq('initScripts.js'))).thenReturn('pluginResourcesPath/initScripts.js')
    }

    @Test
    void 'page is configured with required resources'() {
        GradleInitScriptsPage page = new GradleInitScriptsPage(places, descriptor, scriptsManager)

        assertThat(page.getTabTitle(), equalTo('Gradle Init Scripts'))
        assertThat(page.getIncludeUrl(), equalTo('pluginResourcesPath/projectPage.jsp'))
        assertThat(page.getJsPaths(), hasSize(1))
        assertThat(page.getJsPaths(), hasItem('pluginResourcesPath/initScripts.js'))
        assertThat(page.getCssPaths(), hasSize(1))
        assertThat(page.getCssPaths(), hasItem('/css/admin/buildTypeForm.css'))
    }

    @Test
    void 'page title shows scripts count'() {
        GradleInitScriptsPage page = new GradleInitScriptsPage(places, descriptor, scriptsManager)

        HttpServletRequest request = mock(HttpServletRequest)
        SProject project = mock(SProject)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(scriptsManager.getScriptsCount(eq(project))).thenReturn(2)

        assertThat(page.getTabTitle(request), equalTo('Gradle Init Scripts (2)'))
    }

    @Test
    void 'page fills model with list of scripts'() {
        GradleInitScriptsPage page = new GradleInitScriptsPage(places, descriptor, scriptsManager)

        HttpServletRequest request = mock(HttpServletRequest)
        SProject project = mock(SProject)
        when(request.getAttribute(EditProjectTab.CURRENT_PROJECT_ATTRIBUTE)).thenReturn(project)
        when(scriptsManager.getScriptNames(eq(project))).thenReturn([(project): ['init1.gradle', 'init2.gradle']])
        Map<String, Object> model = [:]

        page.fillModel(model, request)

        assertThat(model, hasKey('scripts'))
        Map<SProject, List<String>> scripts = model.get('scripts') as Map
        assertThat(scripts.get(project), hasItem('init1.gradle'))
        assertThat(scripts.get(project), hasItem('init2.gradle'))
    }
}
