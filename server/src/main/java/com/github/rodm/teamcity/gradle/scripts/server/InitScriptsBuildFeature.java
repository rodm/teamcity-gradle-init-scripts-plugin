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

import jetbrains.buildServer.serverSide.BuildFeature;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE;
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME;

public class InitScriptsBuildFeature extends BuildFeature {

    private PluginDescriptor descriptor;

    public InitScriptsBuildFeature(@NotNull final PluginDescriptor descriptor) {
        this.descriptor = descriptor;
    }

    @Override
    public String getType() {
        return FEATURE_TYPE;
    }

    @Override
    public String getDisplayName() {
        return "Gradle Init Script";
    }

    @Override
    public String getEditParametersUrl() {
        return descriptor.getPluginResourcesPath("editFeature.jsp");
    }

    @Override
    public boolean isMultipleFeaturesPerBuildTypeAllowed() {
        return false;
    }

    @Override
    public String describeParameters(Map<String, String> params) {
        String initScriptName = params.get(INIT_SCRIPT_NAME);
        return "Runs Gradle build steps with the '" + initScriptName + "' initialization script";
    }
}
