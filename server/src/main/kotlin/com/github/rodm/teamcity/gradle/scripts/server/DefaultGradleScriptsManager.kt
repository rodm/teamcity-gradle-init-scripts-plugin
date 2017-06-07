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

import com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.PLUGIN_NAME
import jetbrains.buildServer.log.Loggers.SERVER_CATEGORY
import jetbrains.buildServer.serverSide.CopiedObjects
import jetbrains.buildServer.serverSide.CustomSettingsMapper
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.util.ExceptionUtil
import jetbrains.buildServer.util.FileUtil
import org.apache.log4j.Logger
import java.io.File
import java.io.IOException
import java.lang.Exception
import kotlin.reflect.KFunction1

class DefaultGradleScriptsManager : GradleScriptsManager, CustomSettingsMapper {

    private val LOG = Logger.getLogger(SERVER_CATEGORY + ".GradleInitScripts")

    override fun mapData(copiedObjects: CopiedObjects) {
        for (entry in copiedObjects.copiedProjectsMap.entries) {
            val source = entry.key
            val target = entry.value
            val sourceDir = source.getPluginDataDirectory(PLUGIN_NAME)
            val files = sourceDir.listFiles()
            if (files != null && files.size > 0) {
                var targetDir: File?
                try {
                    targetDir = FileUtil.createDir(target.getPluginDataDirectory(PLUGIN_NAME))
                } catch (e: IOException) {
                    LOG.warn("Could not create directory for project Gradle init scripts", e)
                    continue
                }
                for (sourceFile in files) {
                    val targetFile = File(targetDir, sourceFile.getName())
                    try {
                        FileUtil.copy(sourceFile, targetFile)
                    } catch (e: IOException) {
                        LOG.warn("Could not copy script file: " + sourceFile.absolutePath + " to: " + targetFile.absolutePath, e)
                    }
                }
            }
        }
    }

    override fun getScriptNames(project: SProject): Map<SProject, List<String>> {
        val foundNames = HashSet<String>()
        val result = LinkedHashMap<SProject, List<String>>()
        val filter: KFunction1<File, Boolean> = File::isFile

        val projectPath = project.projectPath
        val iter = projectPath.listIterator(projectPath.size)

        while (iter.hasPrevious()) {
            val currentProject = iter.previous()

            try {
                val pluginDataDirectory = getPluginDataDirectory(currentProject)
                val files = pluginDataDirectory.listFiles(filter)
                if (files != null && files.size > 0) {
                    val scripts = ArrayList<String>()
                    for (file in files) {
                        if (!foundNames.contains(file.name)) {
                            scripts.add(file.name)
                            foundNames.add(file.name)
                        }
                    }
                    if (scripts.size > 0) {
                        result.put(currentProject, scripts)
                    }
                }
            } catch (e: IOException) {
                LOG.error(e.message)
                result.clear()
            }
        }
        return result
    }

    override fun getScriptsCount(project: SProject): Int {
        return getScriptNames(project).values.stream().mapToInt { it.size }.sum()
    }

    override fun findScript(project: SProject, name: String): String? {
        val projectPath = project.projectPath
        val iter = projectPath.listIterator(projectPath.size)

        while (iter.hasPrevious()) {
            val currentProject = iter.previous()

            try {
                val file = File(getPluginDataDirectory(currentProject), name)
                if (file.exists()) {
                    return FileUtil.readText(file, "UTF-8")
                }
            } catch (e: Exception) {
                ExceptionUtil.rethrowAsRuntimeException(e)
            }
        }
        return null
    }

    override fun deleteScript(project: SProject, name: String): Boolean {
        var result = false
        try {
            val file = File(getPluginDataDirectory(project), name)
            result = FileUtil.delete(file)
        }
        catch (e: IOException) {
            LOG.error(e.message)
        }
        return result
    }

    private fun getPluginDataDirectory(project: SProject): File {
        return FileUtil.createDir(project.getPluginDataDirectory(PLUGIN_NAME))
    }
}
