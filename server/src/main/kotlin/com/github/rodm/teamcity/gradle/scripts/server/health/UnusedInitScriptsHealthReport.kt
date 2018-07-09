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

import com.github.rodm.teamcity.gradle.scripts.server.InitScriptsUsageAnalyzer
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItem
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusItemConsumer
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusReport
import jetbrains.buildServer.serverSide.healthStatus.HealthStatusScope
import jetbrains.buildServer.serverSide.healthStatus.ItemCategory
import jetbrains.buildServer.serverSide.healthStatus.ItemSeverity.INFO
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.healthStatus.HealthStatusItemPageExtension

class UnusedInitScriptsHealthReport(pagePlaces: PagePlaces,
                                    descriptor: PluginDescriptor,
                                    private val analyzer: InitScriptsUsageAnalyzer) : HealthStatusReport()
{
    private val TYPE = "UnusedInitScriptsReport"

    private val CATEGORY = ItemCategory("unused_init_scripts", "Unused Gradle init scripts", INFO)

    init {
        val pageExtension = HealthStatusItemPageExtension(TYPE, pagePlaces)
        pageExtension.includeUrl = descriptor.getPluginResourcesPath("/health/unusedInitScripts.jsp")
        pageExtension.isVisibleOutsideAdminArea = true
        pageExtension.register()
    }

    override fun getType() = TYPE

    override fun getDisplayName() = "Unused Gradle init scripts"

    override fun getCategories() = listOf(CATEGORY)

    override fun canReportItemsFor(scope: HealthStatusScope) : Boolean {
        return scope.isItemWithSeverityAccepted(CATEGORY.severity)
    }

    override fun report(scope: HealthStatusScope, resultConsumer: HealthStatusItemConsumer) {
        for (project in scope.projects) {
            val usage = analyzer.getProjectScriptsUsage(project)
            for ((scriptName, scriptUsage) in usage)  {
                if (scriptUsage.getBuildTypes().isEmpty() && scriptUsage.getBuildTemplates().isEmpty()) {
                    val data = mapOf("project" to project, "scriptName" to scriptName)
                    val identity = CATEGORY.id + "_" + project.projectId + "_" + scriptName
                    val statusItem = HealthStatusItem(identity, CATEGORY, data)
                    resultConsumer.consumeForProject(project, statusItem)
                }
            }
        }
    }
}
