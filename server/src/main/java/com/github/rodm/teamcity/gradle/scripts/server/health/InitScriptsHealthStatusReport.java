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

package com.github.rodm.teamcity.gradle.scripts.server.health;

import com.github.rodm.teamcity.gradle.scripts.server.GradleScriptsManager;
import jetbrains.buildServer.serverSide.BuildTypeTemplate;
import jetbrains.buildServer.serverSide.SBuildFeatureDescriptor;
import jetbrains.buildServer.serverSide.SBuildType;
import jetbrains.buildServer.serverSide.healthStatus.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE;
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME;
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.PLUGIN_NAME;

public class InitScriptsHealthStatusReport extends HealthStatusReport {

    private final GradleScriptsManager scriptsManager;

    private final ItemCategory itemCategory;

    public InitScriptsHealthStatusReport(@NotNull GradleScriptsManager scriptsManager) {
        this.scriptsManager = scriptsManager;
        this.itemCategory = new ItemCategory(PLUGIN_NAME, "Gradle init scripts", ItemSeverity.WARN);
    }

    @NotNull
    @Override
    public String getType() {
        return PLUGIN_NAME;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Gradle Init Scripts";
    }

    @NotNull
    @Override
    public Collection<ItemCategory> getCategories() {
        List<ItemCategory> result = new ArrayList<>();
        result.add(itemCategory);
        return result;
    }

    @Override
    public boolean canReportItemsFor(@NotNull HealthStatusScope scope) {
        return scope.isItemWithSeverityAccepted(itemCategory.getSeverity());
    }

    @Override
    public void report(@NotNull HealthStatusScope scope, @NotNull HealthStatusItemConsumer resultConsumer) {
        for (SBuildType buildType : scope.getBuildTypes()) {
            for (SBuildFeatureDescriptor feature : buildType.getBuildFeaturesOfType(FEATURE_TYPE)) {
                Map<String, String> parameters = feature.getParameters();
                String scriptName = parameters.get(INIT_SCRIPT_NAME);
                String scriptContents = scriptsManager.findScript(buildType.getProject(), scriptName);
                if (scriptContents == null) {
                    Set<String> errors = new HashSet<>();
                    errors.add(scriptName);
                    Map<String, Object> data = new HashMap<>();
                    data.put("buildType", buildType);
                    data.put("errors", errors);
                    String identity = "build_type_missing_init_script_" + buildType.getFullName();
                    HealthStatusItem statusItem = new HealthStatusItem(identity, itemCategory, data);
                    resultConsumer.consumeForBuildType(buildType, statusItem);
                }
            }
        }
        for (BuildTypeTemplate buildTemplate : scope.getBuildTypeTemplates()) {
            for (SBuildFeatureDescriptor feature : buildTemplate.getBuildFeaturesOfType(FEATURE_TYPE)) {
                Map<String, String> parameters = feature.getParameters();
                String scriptName = parameters.get(INIT_SCRIPT_NAME);
                String scriptContents = scriptsManager.findScript(buildTemplate.getProject(), scriptName);
                if (scriptContents == null) {
                    Set<String> errors = new HashSet<>();
                    errors.add(scriptName);
                    Map<String, Object> data = new HashMap<>();
                    data.put("buildTemplate", buildTemplate);
                    data.put("errors", errors);
                    String identity = "build_template_missing_init_script_" + buildTemplate.getFullName();
                    HealthStatusItem statusItem = new HealthStatusItem(identity, itemCategory, data);
                    resultConsumer.consumeForTemplate(buildTemplate, statusItem);
                }
            }
        }
    }
}
