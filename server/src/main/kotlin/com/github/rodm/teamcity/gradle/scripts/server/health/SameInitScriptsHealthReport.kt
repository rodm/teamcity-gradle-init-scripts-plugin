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
import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME_PARAMETER
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItemConsumer
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusReport
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusScope
import jetbrains.buildServer.serverSide.healthStatus.ItemCategory
import jetbrains.buildServer.serverSide.healthStatus.ItemSeverity.WARN
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension

class SameInitScriptsHealthReport(pagePlaces: PagePlaces, descriptor: PluginDescriptor) : HealthStatusReport() {

    private val TYPE = "SameInitScriptsReport"

    private val CATEGORY = ItemCategory("same_init_scripts", "Same Gradle init script selected in build step and build feature", WARN)

    init {
        val pageExtension =  HealthStatusItemPageExtension(TYPE, pagePlaces)
        pageExtension.includeUrl = descriptor.getPluginResourcesPath("/health/sameInitScripts.jsp")
        pageExtension.isVisibleOutsideAdminArea = true
        pageExtension.addCssFile("/css/admin/buildTypeForm.css")
        pageExtension.register()
    }

    override fun getType() = TYPE

    override fun getDisplayName() = "Same Gradle init script selected in build step and build feature"

    override fun getCategories() = listOf(CATEGORY)

    override fun canReportItemsFor(scope: HealthStatusScope) : Boolean {
        return scope.isItemWithSeverityAccepted(CATEGORY.severity)
    }

    override fun report(scope: HealthStatusScope, resultConsumer: HealthStatusItemConsumer) {
        for (buildType in scope.buildTypes) {
            for (feature in buildType.getBuildFeaturesOfType(FEATURE_TYPE)) {
                val featureScriptName = feature.parameters[INIT_SCRIPT_NAME]
                buildType.buildRunners.forEach { runner ->
                    val scriptName = runner.parameters[INIT_SCRIPT_NAME_PARAMETER]
                    if (scriptName != null && scriptName == featureScriptName) {
                        val data = mapOf("buildType" to buildType, "scriptName" to scriptName)
                        val identity = CATEGORY.id + "_" + buildType.buildTypeId
                        val statusItem = HealthStatusItem(identity, CATEGORY, data)
                        resultConsumer.consumeForBuildType(buildType, statusItem)
                    }
                }
            }
        }
        for (buildTemplate in scope.buildTypeTemplates) {
            for (feature in buildTemplate.getBuildFeaturesOfType(FEATURE_TYPE)) {
                val featureScriptName = feature.parameters[INIT_SCRIPT_NAME]
                buildTemplate.buildRunners.forEach { runner ->
                    val scriptName = runner.parameters[INIT_SCRIPT_NAME_PARAMETER]
                    if (scriptName != null && scriptName == featureScriptName) {
                        val data = mapOf("buildTemplate" to buildTemplate, "scriptName" to scriptName)
                        val identity = CATEGORY.id + "_" + buildTemplate.id
                        val statusItem = HealthStatusItem(identity, CATEGORY, data)
                        resultConsumer.consumeForTemplate(buildTemplate, statusItem)
                    }
                }
            }
        }
    }
}
