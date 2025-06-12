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

import jetbrains.buildServer.serverSide.SProject

interface GradleScriptsManager {

    fun getScriptNames(project: SProject): Map<SProject, List<String>>

    fun getScriptsCount(project: SProject): Int

    fun findScript(project: SProject, name: String): String?

    fun saveScript(project: SProject, name: String, content: String): Boolean

    fun deleteScript(project: SProject, name: String): Boolean
}
