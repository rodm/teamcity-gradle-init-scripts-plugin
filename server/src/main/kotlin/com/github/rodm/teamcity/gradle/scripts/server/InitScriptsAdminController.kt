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

import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.log.Loggers.SERVER_CATEGORY
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuildServer
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.apache.log4j.Logger
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class InitScriptsAdminController(server: SBuildServer,
                                 val projectManager: ProjectManager,
                                 val pluginDescriptor: PluginDescriptor,
                                 controllerManager: WebControllerManager,
                                 val scriptsManager: GradleScriptsManager) : BaseController(server)
{
    private val log = Logger.getLogger("$SERVER_CATEGORY.GradleInitScripts")

    init {
        controllerManager.registerController("/admin/initScripts.html", this)
    }

    override fun doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView {
        val model = ModelAndView(pluginDescriptor.getPluginResourcesPath("initScriptChooser.jsp"))
        model.addObject("scripts", getScriptNames(request))
        model.addObject("chooserName", getChooserName(request))
        model.addObject("chooserId", "gradleInitScript")
        model.addObject("missingScript", getMissingName(request))
        return model
    }

    private fun getScriptNames(request: HttpServletRequest): Map<SProject, List<String>> {
        val projectId = request.getParameter("projectId")
        if (projectId == null) {
            log.error("Missing request parameter 'projectId'")
            return emptyMap()
        }
        val project = projectManager.findProjectById(projectId)
        if (project == null) {
            log.error("Project not found: projectId: $projectId")
            return emptyMap()
        }
        return scriptsManager.getScriptNames(project)
    }

    private fun getChooserName(request: HttpServletRequest): String {
        val name = request.getParameter("chooserName")
        return name ?: INIT_SCRIPT_NAME
    }

    private fun getMissingName(request: HttpServletRequest): String? {
        val project = getProject(request)
        if (project != null) {
            val selectedScript = request.getAttribute("selectedScript") as String?
            if (selectedScript != null) {
                scriptsManager.findScript(project, selectedScript) ?: return selectedScript
            }
        }
        return null
    }

    private fun getProject(request: HttpServletRequest): SProject? {
        val projectId = request.getParameter("projectId")
        if (projectId == null) {
            log.error("Missing request parameter 'projectId'")
            return null
        }
        return projectManager.findProjectById(projectId)
    }
}
