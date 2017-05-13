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

import jetbrains.buildServer.controllers.admin.projects.EditProjectTab;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.PLUGIN_NAME;

public class GradleInitScriptsPage extends EditProjectTab {

    private static String TITLE = "Gradle Init Scripts";

    private GradleScriptsManager scriptsManager;

    GradleInitScriptsPage(@NotNull final PagePlaces pagePlaces,
                          @NotNull final PluginDescriptor descriptor,
                          @NotNull final GradleScriptsManager scriptsManager)
    {
        super(pagePlaces, PLUGIN_NAME, descriptor.getPluginResourcesPath("projectPage.jsp"), TITLE);
        this.scriptsManager = scriptsManager;
        addCssFile("/css/admin/buildTypeForm.css");
        addJsFile(descriptor.getPluginResourcesPath("initScripts.js"));
    }

    @Override
    public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
        super.fillModel(model, request);
        final SProject project = getProject(request);
        if (project != null) {
            String fileName = request.getParameter("file");
            if (fileName != null) {
                String fileContent = scriptsManager.findScript(project, fileName);
                if (fileContent != null) {
                    model.put("fileName", fileName);
                    model.put("fileContent", fileContent);
                }
            }
            model.put("scripts", scriptsManager.getScriptNames(project));
        }
    }

    @NotNull
    @Override
    public String getTabTitle(@NotNull final HttpServletRequest request) {
        final SProject project = getProject(request);
        String result = TITLE;
        if (project != null) {
            int count =  scriptsManager.getScriptsCount(project);
            if (count > 0) {
                result = TITLE + " (" + count + ")";
            }
        }
        return result;
    }
}
