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
import jetbrains.buildServer.serverSide.auth.Permission
import jetbrains.buildServer.serverSide.auth.SecurityContext
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PluginDescriptor
import javax.servlet.http.HttpServletRequest

private const val TITLE = "Gradle Init Scripts"

class GradleInitScriptsPage(pagePlaces: PagePlaces,
                            descriptor: PluginDescriptor,
                            val scriptsManager: GradleScriptsManager,
                            private val inspector: ProjectInspector,
                            private val securityContext: SecurityContext) :
        EditProjectTab(pagePlaces, descriptor.pluginName, descriptor.getPluginResourcesPath("projectPage.jsp"), "")
{
    init {
        tabTitle = TITLE
        addCssFile("/css/admin/buildTypeForm.css")
        addJsFile(descriptor.getPluginResourcesPath("initScripts.js"))
    }

    override fun fillModel(model: MutableMap<String, Any?>, request: HttpServletRequest) {
        super.fillModel(model, request)
        val project = getProject(request)
        if (project != null) {
            val fileName = request.getParameter("file")
            if (fileName != null && hasPermission(project.projectId)) {
                val fileContent = scriptsManager.findScript(project, fileName)
                if (fileContent != null) {
                    model["fileName"] = fileName
                    model["fileContent"] = fileContent
                }
            }
            val scripts = scriptsManager.getScriptNames(project)
            model["scripts"] = scripts[project]
            val inspections = inspector.report(project)
            model["inspections"] = inspections
        }
    }

    override fun getTabTitle(request: HttpServletRequest): String {
        val project = getProject(request)
        if (project != null) {
            val count =  scriptsManager.getScriptsCount(project)
            if (count > 0) {
                return "$TITLE ($count)"
            }
        }
        return TITLE
    }

    override fun getEditProjectTabGroup(): String {
        return INTEGRATIONS_GROUP
    }

    private fun hasPermission(projectId: String): Boolean {
        return securityContext.authorityHolder.isPermissionGrantedForProject(projectId, Permission.EDIT_PROJECT)
    }
}
