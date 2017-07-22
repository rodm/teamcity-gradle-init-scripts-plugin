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
import com.github.rodm.teamcity.gradle.scripts.server.GradleScriptsManager
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItemConsumer
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusReport
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusScope
import jetbrains.buildServer.serverSide.healthStatus.ItemCategory
import jetbrains.buildServer.serverSide.healthStatus.ItemSeverity.WARN
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension
import java.util.Collections
import java.util.HashMap

class MissingInitScriptsHealthReport(private val scriptsManager: GradleScriptsManager,
                                     pagePlaces: PagePlaces,
                                     descriptor: PluginDescriptor) : HealthStatusReport()
{
    private val TYPE = "MissingInitScriptsReport"

    private val CATEGORY = ItemCategory("missing_init_scripts", "Missing Gradle init scripts", WARN)

    init {
        val pageExtension = HealthStatusItemPageExtension(TYPE, pagePlaces)
        pageExtension.setIncludeUrl(descriptor.getPluginResourcesPath("/health/missingInitScripts.jsp"))
        pageExtension.addCssFile("/css/admin/buildTypeForm.css")
        pageExtension.setVisibleOutsideAdminArea(true)
        pageExtension.register()
    }

    override fun getType() = TYPE

    override fun getDisplayName() = "Missing Gradle Init Scripts"

    override fun getCategories() = Collections.singletonList(CATEGORY)

    override fun canReportItemsFor(scope: HealthStatusScope): Boolean {
        return scope.isItemWithSeverityAccepted(CATEGORY.severity)
    }

    override fun report(scope: HealthStatusScope, resultConsumer: HealthStatusItemConsumer) {
        for (buildType in scope.buildTypes) {
            for (runner in buildType.buildRunners) {
                val parameters = runner.parameters
                val scriptName = parameters[INIT_SCRIPT_NAME_PARAMETER]
                if (scriptName != null) {
                    val scriptContents = scriptsManager.findScript(buildType.project, scriptName)
                    if (scriptContents == null) {
                        val data = HashMap<String, Any?>()
                        data.put("buildType", buildType)
                        data.put("scriptName", scriptName)
                        val identity = CATEGORY.id + "_" + buildType.buildTypeId
                        val statusItem = HealthStatusItem(identity, CATEGORY, data)
                        resultConsumer.consumeForBuildType(buildType, statusItem)
                    }
                }
            }
            for (feature in buildType.getBuildFeaturesOfType(FEATURE_TYPE)) {
                val parameters = feature.parameters
                val scriptName = parameters[INIT_SCRIPT_NAME]
                val scriptContents = scriptsManager.findScript(buildType.project, scriptName!!)
                if (scriptContents == null) {
                    val data = HashMap<String, Any?>()
                    data.put("buildType", buildType)
                    data.put("scriptName", scriptName)
                    val identity = CATEGORY.id + "_" + buildType.buildTypeId
                    val statusItem = HealthStatusItem(identity, CATEGORY, data)
                    resultConsumer.consumeForBuildType(buildType, statusItem)
                }
            }
        }
        for (buildTemplate in scope.buildTypeTemplates) {
            for (runner in buildTemplate.buildRunners) {
                val parameters = runner.parameters
                val scriptName = parameters[INIT_SCRIPT_NAME_PARAMETER]
                if (scriptName != null) {
                    val scriptContents = scriptsManager.findScript(buildTemplate.project, scriptName)
                    if (scriptContents == null) {
                        val data = HashMap<String, Any?>()
                        data.put("buildTemplate", buildTemplate)
                        data.put("scriptName", scriptName)
                        val identity = CATEGORY.id + "_" + buildTemplate.templateId
                        val statusItem = HealthStatusItem(identity, CATEGORY, data)
                        resultConsumer.consumeForTemplate(buildTemplate, statusItem)
                    }
                }
            }
            for (feature in buildTemplate.getBuildFeaturesOfType(FEATURE_TYPE)) {
                val parameters = feature.parameters
                val scriptName = parameters[INIT_SCRIPT_NAME]
                val scriptContents = scriptsManager.findScript(buildTemplate.project, scriptName!!)
                if (scriptContents == null) {
                    val data = HashMap<String, Any?>()
                    data.put("buildTemplate", buildTemplate)
                    data.put("scriptName", scriptName)
                    val identity = CATEGORY.id + "_" + buildTemplate.templateId
                    val statusItem = HealthStatusItem(identity, CATEGORY, data)
                    resultConsumer.consumeForTemplate(buildTemplate, statusItem)
                }
            }
        }
    }
}
