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
import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME
import jetbrains.buildServer.serverSide.BuildFeature
import jetbrains.buildServer.web.openapi.PluginDescriptor

class InitScriptsBuildFeature(private val descriptor: PluginDescriptor) : BuildFeature() {

    override  fun getType() = FEATURE_TYPE

    override fun getDisplayName() = "Gradle Init Script"

    override fun getEditParametersUrl() = descriptor.getPluginResourcesPath("editFeature.jsp")

    override fun isMultipleFeaturesPerBuildTypeAllowed() = false

    override fun describeParameters(params: MutableMap<String, String>): String {
        val initScriptName = params[INIT_SCRIPT_NAME]
        return "Runs Gradle build steps with the '$initScriptName' initialization script"
    }
}
