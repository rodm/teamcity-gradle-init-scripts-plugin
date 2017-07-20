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

import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE
import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_CONTENT
import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_CONTENT_PARAMETER
import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME
import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME_PARAMETER
import jetbrains.buildServer.log.Loggers.SERVER_CATEGORY
import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.BuildStartContextProcessor
import jetbrains.buildServer.serverSide.SRunnerContext
import org.apache.log4j.Logger

open class InitScriptsProvider(private val scriptsManager: GradleScriptsManager) : BuildStartContextProcessor {

    private val LOG = Logger.getLogger(SERVER_CATEGORY + ".GradleInitScripts")

    override fun updateParameters(context: BuildStartContext) {
        for (runnerContext in context.runnerContexts) {
            if (isGradleRunner(runnerContext)) {
                val buildType = context.build.buildType
                if (buildType != null) {
                    val project = buildType.project

                    var scriptName = runnerContext.parameters[INIT_SCRIPT_NAME_PARAMETER]
                    if (scriptName != null) {
                        val scriptContent = scriptsManager.findScript(project, scriptName)
                        if (scriptContent != null) {
                            runnerContext.addRunnerParameter(INIT_SCRIPT_NAME_PARAMETER, scriptName)
                            runnerContext.addRunnerParameter(INIT_SCRIPT_CONTENT_PARAMETER, scriptContent)
                        } else {
                            LOG.error("Init script '$scriptName' not found")
                        }
                    }

                    val features = buildType.getBuildFeaturesOfType(FEATURE_TYPE)
                    for (feature in features) {
                        scriptName = feature.parameters[INIT_SCRIPT_NAME]
                        val scriptContent = scriptsManager.findScript(project, scriptName!!)
                        runnerContext.addRunnerParameter(INIT_SCRIPT_NAME, scriptName)
                        if (scriptContent != null) {
                            runnerContext.addRunnerParameter(INIT_SCRIPT_CONTENT, scriptContent)
                        } else {
                            LOG.error("Init script '$scriptName' not found")
                        }
                    }
                }
            }
        }
    }

    open fun isGradleRunner(runnerContext: SRunnerContext) : Boolean {
        return "gradle-runner" == runnerContext.runType.type
    }
}
