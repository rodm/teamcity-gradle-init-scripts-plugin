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

import jetbrains.buildServer.controllers.ActionErrors
import jetbrains.buildServer.controllers.ActionMessages
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.auth.Permission
import jetbrains.buildServer.serverSide.auth.SecurityContext
import jetbrains.buildServer.util.StringUtil.isEmpty
import jetbrains.buildServer.web.openapi.ControllerAction
import org.jdom.Element
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class DeleteScriptAction(controller: InitScriptsActionsController,
                         val projectManager: ProjectManager,
                         val scriptsManager: GradleScriptsManager,
                         val securityContext: SecurityContext) : ControllerAction
{
    private val NAME = "deleteScript"

    init {
        controller.registerAction(this)
    }

    override fun canProcess(request: HttpServletRequest): Boolean {
        return NAME == request.getParameter("action")
    }

    override fun process(request: HttpServletRequest, response: HttpServletResponse, ajaxResponse: Element?) {
        val name = request.getParameter("name")
        val projectId = request.getParameter("projectId")
        if (isEmpty(name) || isEmpty(projectId)) {
            return
        }
        val actionErrors = ActionErrors()
        val project = projectManager.findProjectById(projectId)
        if (project == null) {
            actionErrors.addError("initScriptsError", "File $name cannot be deleted. Project $projectId was not found")
            if (ajaxResponse != null) actionErrors.serialize(ajaxResponse)
            return
        }
        if (!hasPermission(projectId)) {
            actionErrors.addError("initScriptsError", "You do not have permissions to edit project settings")
            if (ajaxResponse != null) actionErrors.serialize(ajaxResponse)
            return
        }
        try {
            val deleted = scriptsManager.deleteScript(project, name)
            val message = "Gradle init script $name ${if (deleted) "was deleted" else "has been scheduled for deletion"}"
            ActionMessages.getOrCreateMessages(request).addMessage("initScriptsMessage", message)
        }
        catch (_: Throwable) {
            actionErrors.addError("initScriptsError", "Gradle init script $name cannot be deleted.")
            if (ajaxResponse != null) actionErrors.serialize(ajaxResponse)
        }
    }

    private fun hasPermission(projectId: String): Boolean {
        return securityContext.authorityHolder.isPermissionGrantedForProject(projectId, Permission.EDIT_PROJECT)
    }
}
