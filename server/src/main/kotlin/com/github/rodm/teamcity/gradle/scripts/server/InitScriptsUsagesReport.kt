/*
 * Copyright 2024 Rod MacKenzie.
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

import jetbrains.buildServer.controllers.admin.projects.UsagesReportPageExtension
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PluginDescriptor
import javax.servlet.http.HttpServletRequest

private const val SCRIPT_NAME = "scriptName"

class InitScriptsUsagesReport(pagePlaces: PagePlaces,
                              descriptor: PluginDescriptor,
                              private val analyzer: InitScriptsUsageAnalyzer)
    : UsagesReportPageExtension("gradleInitScripts", pagePlaces)
{
    init {
        includeUrl = descriptor.getPluginResourcesPath("usagesReport.jsp")
        register()
    }

    override fun fillModel(model: MutableMap<String, Any>, request: HttpServletRequest) {
        super.fillModel(model, request)
        val project = getProject(request)
        val scriptName = request.getParameter(SCRIPT_NAME)
        model["currentProject"] = project
        model[SCRIPT_NAME] = scriptName

        val usages = analyzer.getProjectScriptsUsage(project).getOrDefault(scriptName, ScriptUsage())
        model["buildTypeUsages"] = usages.getBuildTypes().toList()
        model["templateUsages"] = usages.getBuildTemplates().toList()
    }

    override fun isAvailable(request: HttpServletRequest): Boolean {
        return super.isAvailable(request) && request.getParameter(SCRIPT_NAME) != null
    }
}
