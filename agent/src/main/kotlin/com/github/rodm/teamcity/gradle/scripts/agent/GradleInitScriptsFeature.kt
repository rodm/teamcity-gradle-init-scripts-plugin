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

package com.github.rodm.teamcity.gradle.scripts.agent

import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE
import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_CONTENT
import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.log.Loggers.AGENT_CATEGORY
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.FileUtil
import org.apache.log4j.Logger
import java.io.File
import java.io.IOException

open class GradleInitScriptsFeature(eventDispatcher: EventDispatcher<AgentLifeCycleListener>) : AgentLifeCycleAdapter() {

    private val LOG = Logger.getLogger(AGENT_CATEGORY + ".GradleInitScriptsFeature")

    private val GRADLE_CMD_PARAMS = "ui.gradleRunner.additional.gradle.cmd.params"

    private var initScriptFile: File? = null

    init {
        eventDispatcher.addListener(this)
    }

    override fun beforeRunnerStart(runner: BuildRunnerContext) {
        if (hasGradleInitScriptFeature(runner)) {
            val runnerParameters = runner.runnerParameters
            val initScriptName = runnerParameters.get(INIT_SCRIPT_NAME)
            val initScriptContent = runnerParameters.get(INIT_SCRIPT_CONTENT)

            if (initScriptContent == null) {
                throw RuntimeException("Runner is configured to use init script '$initScriptName', but no content was found. Please check runner settings.")
            }

            try {
                initScriptFile = FileUtil.createTempFile(getBuildTempDirectory(runner), "init_", ".gradle", true)
                FileUtil.writeFile(initScriptFile!!, initScriptContent, "UTF-8")

                val params = runnerParameters.getOrDefault(GRADLE_CMD_PARAMS, "")
                val initScriptParams = "--init-script " + initScriptFile!!.absolutePath
                runner.addRunnerParameter(GRADLE_CMD_PARAMS, initScriptParams + " " + params)
            } catch (e: IOException) {
                LOG.info("Failed to write init script: " + e.message)
            }
        }
    }

    override fun runnerFinished(runner: BuildRunnerContext, status: BuildFinishedStatus) {
        if (initScriptFile != null) {
            FileUtil.delete(initScriptFile!!)
            initScriptFile = null
        }
    }

    open fun hasGradleInitScriptFeature(context: BuildRunnerContext) : Boolean {
        return !context.build.getBuildFeaturesOfType(FEATURE_TYPE).isEmpty()
    }

    open fun getBuildTempDirectory(context: BuildRunnerContext) : File {
        return context.build.getBuildTempDirectory()
    }
}
