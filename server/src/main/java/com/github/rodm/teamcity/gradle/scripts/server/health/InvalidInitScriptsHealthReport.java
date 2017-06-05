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

import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.healthStatus.*;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE;
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME;
import static jetbrains.buildServer.serverSide.healthStatus.ItemSeverity.INFO;

public class InvalidInitScriptsHealthReport extends HealthStatusReport {

    private static final String TYPE = "InvalidInitScriptsReport";

    private final ItemCategory CATEGORY = new ItemCategory("invalid_init_scripts", "Invalid Gradle init scripts configuration", INFO);

    public InvalidInitScriptsHealthReport(@NotNull PagePlaces pagePlaces, @NotNull PluginDescriptor descriptor) {
        final HealthStatusItemPageExtension pageExtension = new HealthStatusItemPageExtension(TYPE, pagePlaces);
        pageExtension.setIncludeUrl(descriptor.getPluginResourcesPath("/health/invalidInitScripts.jsp"));
        pageExtension.addCssFile("/css/admin/buildTypeForm.css");
        pageExtension.setVisibleOutsideAdminArea(true);
        pageExtension.register();
    }

    @NotNull
    @Override
    public String getType() {
        return TYPE;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return "Invalid Gradle Init Scripts configuration";
    }

    @NotNull
    @Override
    public Collection<ItemCategory> getCategories() {
        return Collections.singletonList(CATEGORY);
    }

    @Override
    public boolean canReportItemsFor(@NotNull HealthStatusScope scope) {
        return scope.isItemWithSeverityAccepted(CATEGORY.getSeverity());
    }

    @Override
    public void report(@NotNull HealthStatusScope scope, @NotNull HealthStatusItemConsumer resultConsumer) {
        for (SBuildType buildType : scope.getBuildTypes()) {
            for (SBuildFeatureDescriptor feature : buildType.getBuildFeaturesOfType(FEATURE_TYPE)) {
                if (!hasGradleRunnerBuildStep(buildType)) {
                    Map<String, String> parameters = feature.getParameters();
                    String scriptName = parameters.get(INIT_SCRIPT_NAME);
                    Map<String, Object> data = new HashMap<>();
                    data.put("buildType", buildType);
                    data.put("scriptName", scriptName);
                    String identity = CATEGORY.getId() + "_" + buildType.getBuildTypeId();
                    HealthStatusItem statusItem = new HealthStatusItem(identity, CATEGORY, data);
                    resultConsumer.consumeForBuildType(buildType, statusItem);
                }
            }
        }
        for (BuildTypeTemplate buildTemplate : scope.getBuildTypeTemplates()) {
            for (SBuildFeatureDescriptor feature : buildTemplate.getBuildFeaturesOfType(FEATURE_TYPE)) {
                if (!hasGradleRunnerBuildStep(buildTemplate)) {
                    Map<String, String> parameters = feature.getParameters();
                    String scriptName = parameters.get(INIT_SCRIPT_NAME);
                    Map<String, Object> data = new HashMap<>();
                    data.put("buildTemplate", buildTemplate);
                    data.put("scriptName", scriptName);
                    String identity = CATEGORY.getId() + "_" + buildTemplate.getTemplateId();
                    HealthStatusItem statusItem = new HealthStatusItem(identity, CATEGORY, data);
                    resultConsumer.consumeForTemplate(buildTemplate, statusItem);
                }
            }
        }
    }

    boolean hasGradleRunnerBuildStep(BuildTypeSettings buildTypeSettings) {
        for (SBuildRunnerDescriptor runner : buildTypeSettings.getBuildRunners()) {
            if ("gradle-runner".equals(runner.getRunType().getType())) {
                return true;
            }
        }
        return false;
    }
}
