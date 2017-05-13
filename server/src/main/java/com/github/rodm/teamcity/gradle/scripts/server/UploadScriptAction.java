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

package com.github.rodm.teamcity.gradle.scripts.server;

import jetbrains.buildServer.controllers.MultipartFormController;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.PLUGIN_NAME;

public class UploadScriptAction extends MultipartFormController {

    private final ProjectManager projectManager;

    public UploadScriptAction(@NotNull ProjectManager projectManager, @NotNull WebControllerManager controllerManager) {
        this.projectManager = projectManager;
        controllerManager.registerController("/admin/uploadInitScript.html", this);
    }

    protected ModelAndView doPost(HttpServletRequest request, HttpServletResponse response) {
        ModelAndView modelAndView = new ModelAndView("/_fileUploadResponse.jsp");
        Map<String, Object> model = modelAndView.getModel();
        model.put("jsBase", "BS.GradleAddInitScripts");
        String fileName = request.getParameter("fileName");
        if (StringUtil.isEmpty(fileName)) {
            model.put("error", "File name must be provided");
            return modelAndView;
        }

        SProject project = projectManager.findProjectByExternalId(request.getParameter("project"));
        if (project == null) {
            model.put("error", "Cannot upload file. Project is missing");
            return modelAndView;
        }

        try {
            MultipartFile file = getMultipartFileOrFail(request, "file:fileToUpload");
            if (file == null) {
                model.put("error", "No file set");
                return modelAndView;
            }

            File pluginDataDirectory = FileUtil.createDir(project.getPluginDataDirectory(PLUGIN_NAME));
            File destinationFile = new File(pluginDataDirectory, fileName);
            file.transferTo(destinationFile);
        }
        catch (IOException | IllegalStateException e) {
             model.put("error", e.getMessage());
        }
        return modelAndView;
    }
}
