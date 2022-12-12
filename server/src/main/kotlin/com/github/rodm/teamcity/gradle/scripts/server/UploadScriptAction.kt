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
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import java.io.IOException
import java.lang.IllegalStateException
import java.nio.file.Paths

class UploadScriptAction(val projectManager: ProjectManager,
                         controllerManager: WebControllerManager,
                         val scriptsManager: GradleScriptsManager) : MultipartFormController()
{
    init {
        controllerManager.registerController("/admin/uploadInitScript.html", this)
    }

    override fun doPost(request: HttpServletRequest, response: HttpServletResponse): ModelAndView {
        val modelAndView = ModelAndView("/_fileUploadResponse.jsp")
        val model = modelAndView.model
        model.put("jsBase", "BS.GradleAddInitScripts")
        val fileName = request.getParameter("fileName")
        if (StringUtil.isEmpty(fileName)) {
            model.put("error", "File name must be provided")
            return modelAndView
        }
        if (!validFileName(fileName)) {
            model.put("error", "Invalid file name provided for init script")
            return modelAndView
        }
        if (!validFileNameExtension(fileName)) {
            model.put("error", "Invalid extension provided for init script")
            return modelAndView
        }

        val project = projectManager.findProjectByExternalId(request.getParameter("project"))
        if (project == null) {
            model.put("error", "Cannot upload file. Project is missing")
            return modelAndView
        }

        try {
            val file = getMultipartFileOrFail(request, "file:fileToUpload")
            if (file == null) {
                model.put("error", "No file set")
                return modelAndView
            }

            val exists = scriptsManager.findScript(project, fileName) != null
            val message = "Gradle init script $fileName was ${if (exists) "updated" else "uploaded"}"
            val content = String(file.bytes, Charsets.UTF_8)
            scriptsManager.saveScript(project, fileName, content)
            ActionMessages.getOrCreateMessages(request).addMessage("initScriptsMessage", message)
        }
        catch (e: IOException) {
             model.put("error", e.message)
        }
        catch (e: IllegalStateException) {
             model.put("error", e.message)
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
}
