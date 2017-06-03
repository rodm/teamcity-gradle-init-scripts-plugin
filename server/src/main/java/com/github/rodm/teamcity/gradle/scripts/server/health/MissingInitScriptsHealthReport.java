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
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.FEATURE_TYPE;
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME;
import static jetbrains.buildServer.serverSide.healthStatus.ItemSeverity.WARN;

public class MissingInitScriptsHealthReport extends HealthStatusReport {

    private static final String TYPE = "MissingInitScriptsReport";

    private final GradleScriptsManager scriptsManager;

    private final ItemCategory CATEGORY = new ItemCategory("missing_init_scripts", "Missing Gradle init scripts", WARN);

    public MissingInitScriptsHealthReport(@NotNull GradleScriptsManager scriptsManager,
                                          @NotNull PagePlaces pagePlaces,
                                          @NotNull PluginDescriptor descriptor)
    {
        this.scriptsManager = scriptsManager;
        final HealthStatusItemPageExtension pageExtension = new HealthStatusItemPageExtension(TYPE, pagePlaces);
        pageExtension.setIncludeUrl(descriptor.getPluginResourcesPath("/health/missingInitScripts.jsp"));
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
        return "Missing Gradle Init Scripts";
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
                Map<String, String> parameters = feature.getParameters();
                String scriptName = parameters.get(INIT_SCRIPT_NAME);
                String scriptContents = scriptsManager.findScript(buildType.getProject(), scriptName);
                if (scriptContents == null) {
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
                Map<String, String> parameters = feature.getParameters();
                String scriptName = parameters.get(INIT_SCRIPT_NAME);
                String scriptContents = scriptsManager.findScript(buildTemplate.getProject(), scriptName);
                if (scriptContents == null) {
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
}
