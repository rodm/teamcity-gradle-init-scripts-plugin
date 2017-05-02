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

package teamcity.gradle.scripts.server;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.SBuildServer;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Collections;
import java.util.List;

import static jetbrains.buildServer.log.Loggers.SERVER_CATEGORY;
import static teamcity.gradle.scripts.common.GradleInitScriptsPlugin.INIT_SCRIPT_NAME;

public class InitScriptsAdminController extends BaseController {

    @NotNull
    private static final Logger LOG = Logger.getLogger(SERVER_CATEGORY + ".GradleInitScripts");

    private final ProjectManager projectManager;

    private final GradleScriptsManager scriptsManager;

    private final PluginDescriptor pluginDescriptor;

    public InitScriptsAdminController(@NotNull SBuildServer server,
                                      @NotNull ProjectManager projectManager,
                                      @NotNull PluginDescriptor pluginDescriptor,
                                      @NotNull WebControllerManager controllerManager,
                                      @NotNull GradleScriptsManager manager)
    {
        super(server);
        this.projectManager = projectManager;
        this.scriptsManager = manager;
        this.pluginDescriptor = pluginDescriptor;
        controllerManager.registerController("/admin/initScripts.html", this);
    }

    @Nullable
    protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
        ModelAndView model = new ModelAndView(pluginDescriptor.getPluginResourcesPath("initScriptChooser.jsp"));
        model.addObject("scripts", getScriptNames(request));
        model.addObject("selectedScript", request.getParameter("selectedScript"));
        model.addObject("chooserName", INIT_SCRIPT_NAME);
        model.addObject("chooserId", "gradleInitScript");
        return model;
    }

    @NotNull
    private List<String> getScriptNames(@NotNull HttpServletRequest request) {
        String projectId = request.getParameter("projectId");
        if (projectId == null) {
            LOG.error("Missing request parameter 'projectId'");
            return Collections.emptyList();
        }
        SProject project = projectManager.findProjectById(projectId);
        if (project == null) {
            LOG.error("Project not found: projectId: " + projectId);
            return Collections.emptyList();
        }
        return scriptsManager.getScriptNames(project);
    }
}
