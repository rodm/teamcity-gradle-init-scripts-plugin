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

import jetbrains.buildServer.serverSide.BuildTypeTemplate;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.SProject;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE;
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME;

public class InitScriptsUsageAnalyzer {

    private final GradleScriptsManager scriptsManager;

    InitScriptsUsageAnalyzer(GradleScriptsManager scriptsManager) {
        this.scriptsManager = scriptsManager;
    }

    public Map<String, ScriptUsage> getProjectScriptsUsage(SProject project) {
        Map<SProject, List<String>> scripts = scriptsManager.getScriptNames(project);
        Map<SProject, List<String>> scriptsForCurrentProject = new LinkedHashMap<>();
        scriptsForCurrentProject.put(project, scripts.get(project));
        return getScriptsUsage(scriptsForCurrentProject);
    }

    Map<String, ScriptUsage> getScriptsUsage(Map<SProject, List<String>> scripts) {
        Map<String, ScriptUsage> usage = new LinkedHashMap<>();
        for (Map.Entry<SProject, List<String>> entry : scripts.entrySet()) {
            SProject project = entry.getKey();
            for (String script : entry.getValue()) {
                usage.put(script, new ScriptUsage());
                addScriptUsageForSubProjects(script, project, usage);
            }
        }
        return usage;
    }

    private void addScriptUsageForSubProjects(String name, SProject parent, Map<String, ScriptUsage> usage) {
        for (SProject project : parent.getOwnProjects()) {
            List<String> scripts = getProjectScripts(project);
            if (!scripts.contains(name)) {
                addScriptUsageForSubProjects(name, project, usage);
            }
        }
        addScriptUsageForProject(name, parent, usage);
    }

    private void addScriptUsageForProject(String name, SProject project, Map<String, ScriptUsage> usage) {
        for (SBuildType buildType : project.getOwnBuildTypes()) {
            for (SBuildFeatureDescriptor feature : buildType.getBuildFeaturesOfType(FEATURE_TYPE)) {
                Map<String, String> parameters = feature.getParameters();
                String scriptName = parameters.get(INIT_SCRIPT_NAME);
                if (scriptName.equals(name)) {
                    usage.get(scriptName).addBuildType(buildType);
                }
            }
        }
        for (BuildTypeTemplate buildTemplate : project.getOwnBuildTypeTemplates()) {
            for (SBuildFeatureDescriptor feature : buildTemplate.getBuildFeaturesOfType(FEATURE_TYPE)) {
                Map<String, String> parameters = feature.getParameters();
                String scriptName = parameters.get(INIT_SCRIPT_NAME);
                if (scriptName.equals(name)) {
                    usage.get(scriptName).addBuildTemplate(buildTemplate);
                }
            }
        }
    }

    private List<String> getProjectScripts(SProject project) {
        return scriptsManager.getScriptNames(project).getOrDefault(project, new ArrayList<>());
    }
}
