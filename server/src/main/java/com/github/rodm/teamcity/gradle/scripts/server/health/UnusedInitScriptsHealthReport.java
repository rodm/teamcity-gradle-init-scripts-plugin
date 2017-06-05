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

import com.github.rodm.teamcity.gradle.scripts.server.InitScriptsUsageAnalyzer;
import com.github.rodm.teamcity.gradle.scripts.server.ScriptUsage;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.healthStatus.*;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static jetbrains.buildServer.serverSide.healthStatus.ItemSeverity.INFO;

public class UnusedInitScriptsHealthReport extends HealthStatusReport {

    private static final String TYPE = "UnusedInitScriptsReport";

    private final ItemCategory CATEGORY = new ItemCategory("unused_init_scripts", "Unused Gradle init scripts", INFO);

    private final InitScriptsUsageAnalyzer analyzer;

    public UnusedInitScriptsHealthReport(@NotNull PagePlaces pagePlaces,
                                         @NotNull PluginDescriptor descriptor,
                                         @NotNull InitScriptsUsageAnalyzer analyzer)
    {
        this.analyzer = analyzer;
        final HealthStatusItemPageExtension pageExtension = new HealthStatusItemPageExtension(TYPE, pagePlaces);
        pageExtension.setIncludeUrl(descriptor.getPluginResourcesPath("/health/unusedInitScripts.jsp"));
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
        return "Unused Gradle init scripts";
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
        for (SProject project : scope.getProjects()) {
            Map<String, ScriptUsage> usage = analyzer.getProjectScriptsUsage(project);
            for (Map.Entry<String, ScriptUsage> entry : usage.entrySet()) {
                String scriptName = entry.getKey();
                ScriptUsage scriptUsage = entry.getValue();
                if (scriptUsage.getBuildTypes().isEmpty() && scriptUsage.getBuildTemplates().isEmpty()) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("project", project);
                    data.put("scriptName", scriptName);
                    String identity = CATEGORY.getId() + "_" + project.getProjectId() + "_" + scriptName;
                    HealthStatusItem statusItem = new HealthStatusItem(identity, CATEGORY, data);
                    resultConsumer.consumeForProject(project, statusItem);
                }
            }
        }
    }
}
