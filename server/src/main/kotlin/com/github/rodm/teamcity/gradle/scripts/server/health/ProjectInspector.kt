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

package com.github.rodm.teamcity.gradle.scripts.server.health

import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE
import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME
import com.github.rodm.teamcity.gradle.scripts.server.GradleScriptsManager
import jetbrains.buildServer.serverSide.BuildTypeTemplate
import jetbrains.buildServer.serverSide.SBuildType
import jetbrains.buildServer.serverSide.SProject

class ProjectInspector(val scriptsManager: GradleScriptsManager) {

    fun report(project: SProject) : List<ProjectReport> {
        val result = ArrayList<ProjectReport>()
        val projects = mutableListOf(project)
        projects.addAll(project.projects)

        for (p in projects) {
            inspectProject(p, result)
        }
        return result
    }

    fun inspectProject(project: SProject, result: ArrayList<ProjectReport>) {
        val buildTypes = mutableMapOf<SBuildType, String>()
        val buildTemplates = mutableMapOf<BuildTypeTemplate, String>()

        for (buildType in project.ownBuildTypes) {
            for (feature in buildType.getBuildFeaturesOfType(FEATURE_TYPE)) {
                val scriptName = feature.parameters[INIT_SCRIPT_NAME]
                val scriptContent = scriptsManager.findScript(project, scriptName!!)
                if (scriptContent == null) {
                    buildTypes.put(buildType, scriptName)
                }
            }
        }
        for (buildTemplate in project.ownBuildTypeTemplates) {
            for (feature in buildTemplate.getBuildFeaturesOfType(FEATURE_TYPE)) {
                val scriptName = feature.parameters[INIT_SCRIPT_NAME]
                val scriptContent = scriptsManager.findScript(project, scriptName!!)
                if (scriptContent == null) {
                    buildTemplates.put(buildTemplate, scriptName)
                }
            }
        }
        if (!buildTypes.isEmpty() || !buildTemplates.isEmpty()) {
            result.add(ProjectReport(project, buildTypes, buildTemplates))
        }
    }
}
