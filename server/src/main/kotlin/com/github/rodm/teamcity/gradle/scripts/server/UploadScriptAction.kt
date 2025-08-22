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
import jetbrains.buildServer.controllers.MultipartFormController
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.auth.Permission
import jetbrains.buildServer.serverSide.auth.SecurityContext
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.web.servlet.ModelAndView
import java.io.IOException
import java.nio.file.Paths
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class UploadScriptAction(val projectManager: ProjectManager,
                         controllerManager: WebControllerManager,
                         val scriptsManager: GradleScriptsManager,
                         val securityContext: SecurityContext) : MultipartFormController()
{
    init {
        controllerManager.registerController("/admin/uploadInitScript.html", this)
    }

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse): ModelAndView {
        val modelAndView = ModelAndView("/_fileUploadResponse.jsp")
        val model = modelAndView.model
        model["jsBase"] = "BS.GradleAddInitScripts"
        val fileName = request.getParameter("fileName")
        if (StringUtil.isEmpty(fileName)) {
            model["error"] = "File name must be provided"
            return modelAndView
        }
        if (!validFileName(fileName)) {
            model["error"] = "Invalid file name provided for init script"
            return modelAndView
        }
        if (!validFileNameExtension(fileName)) {
            model["error"] = "Invalid extension provided for init script"
            return modelAndView
        }

        val project = projectManager.findProjectByExternalId(request.getParameter("project"))
        if (project == null) {
            model["error"] = "Cannot upload file. Project is missing"
            return modelAndView
        }
        if (!hasPermission(project.projectId)) {
            model["error"] = "You do not have permissions to edit project settings"
            return modelAndView
        }

        try {
            val file = getMultipartFileOrFail(request, "file:fileToUpload")
            if (file == null) {
                model["error"] = "No file set"
                return modelAndView
            }

            val exists = scriptsManager.findScript(project, fileName) != null
            val content = String(file.bytes, Charsets.UTF_8)
            val saved = scriptsManager.saveScript(project, fileName, content)
            val message = if (saved) {
                "Gradle init script $fileName was ${if (exists) "updated" else "uploaded"}"
            } else {
                "Gradle init script $fileName has been scheduled for ${if (exists) "updating" else "saving"}"
            }
            ActionMessages.getOrCreateMessages(request).addMessage("initScriptsMessage", message)
        }
        catch (e: IOException) {
            model["error"] = e.message
        }
        catch (e: IllegalStateException) {
            model["error"] = e.message
        }
        return modelAndView
    }

    private fun validFileName(name: String): Boolean {
        val path = Paths.get(name)
        return path.equals(path.fileName)
    }

    private fun validFileNameExtension(name: String): Boolean {
        return name.endsWith(".gradle") || name.endsWith(".gradle.kts")
    }

    private fun hasPermission(projectId: String): Boolean {
        return securityContext.authorityHolder.isPermissionGrantedForProject(projectId, Permission.EDIT_PROJECT)
    }
}
