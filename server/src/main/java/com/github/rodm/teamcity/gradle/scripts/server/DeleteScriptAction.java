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

import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.openapi.ControllerAction;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class DeleteScriptAction implements ControllerAction {

    private static final String NAME = "deleteScript";

    private final ProjectManager projectManager;

    private final GradleScriptsManager scriptsManager;

    public DeleteScriptAction(@NotNull InitScriptsActionsController controller,
                              @NotNull ProjectManager projectManager,
                              @NotNull GradleScriptsManager scriptsManager)
    {
        this.projectManager = projectManager;
        this.scriptsManager = scriptsManager;
        controller.registerAction(this);
    }

    public boolean canProcess(@NotNull HttpServletRequest request) {
        return NAME.equals(request.getParameter("action"));
    }

    public void process(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @Nullable Element element) {
        String name = request.getParameter("name");
        if (name != null) {
            String projectId = request.getParameter("projectId");
            SProject project = projectManager.findProjectById(projectId);
            if (project != null) {
                scriptsManager.deleteScript(project, name);
            }
        }
    }
}
