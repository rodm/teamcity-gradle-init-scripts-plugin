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

import jetbrains.buildServer.serverSide.BuildTypeTemplate
import jetbrains.buildServer.serverSide.SBuildType
import java.util.LinkedHashSet

class ScriptUsage {

    private val buildTypes = LinkedHashSet<SBuildType>()

    private val buildTemplates = LinkedHashSet<BuildTypeTemplate>()

    fun getBuildTypes() = buildTypes

    fun addBuildType(buildType: SBuildType) {
        buildTypes.add(buildType)
    }

    fun getBuildTemplates() = buildTemplates

    fun addBuildTemplate(buildTemplate: BuildTypeTemplate) {
        buildTemplates.add(buildTemplate)
    }
}
