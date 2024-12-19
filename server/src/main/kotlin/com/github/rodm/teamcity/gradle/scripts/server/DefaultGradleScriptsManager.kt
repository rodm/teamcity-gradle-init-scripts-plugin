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

import jetbrains.buildServer.log.Loggers.SERVER_CATEGORY
import jetbrains.buildServer.serverSide.ConfigActionFactory
import jetbrains.buildServer.serverSide.ConfigFileChangesListener
import jetbrains.buildServer.serverSide.CopiedObjects
import jetbrains.buildServer.serverSide.CustomSettingsMapper
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.VersionedSettingsRegistry
import jetbrains.buildServer.util.ExceptionUtil
import jetbrains.buildServer.util.FileUtil
import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.apache.log4j.Logger
import java.io.File
import java.io.IOException
import java.lang.Exception
import kotlin.reflect.KFunction1

class DefaultGradleScriptsManager(descriptor: PluginDescriptor,
                                  registry: VersionedSettingsRegistry,
                                  private val configChangesListener: ConfigFileChangesListener,
                                  private val configActionFactory: ConfigActionFactory)
    : GradleScriptsManager, CustomSettingsMapper
{
    private val log = Logger.getLogger("$SERVER_CATEGORY.GradleInitScripts")

    private val pluginName = descriptor.pluginName

    init {
        registry.registerDir("pluginData/" + descriptor.pluginName)
    }

    override fun mapData(copiedObjects: CopiedObjects) {
        for ((source, target) in copiedObjects.copiedProjectsMap) {
            val sourceDir = source.getPluginDataDirectory(pluginName)
            val files = sourceDir.listFiles()
            if (files != null && files.isNotEmpty()) {
                var targetDir: File?
                try {
                    targetDir = FileUtil.createDir(target.getPluginDataDirectory(pluginName))
                } catch (e: IOException) {
                    log.warn("Could not create directory for project Gradle init scripts", e)
                    continue
                }
                for (sourceFile in files) {
                    val targetFile = File(targetDir, sourceFile.name)
                    try {
                        FileUtil.copy(sourceFile, targetFile)
                    } catch (e: IOException) {
                        log.warn("Could not copy script file: " + sourceFile.absolutePath + " to: " + targetFile.absolutePath, e)
                    }
                }
            }
        }
    }

    override fun getScriptNames(project: SProject): Map<SProject, List<String>> {
        val foundNames = HashSet<String>()
        val result = LinkedHashMap<SProject, List<String>>()

        val projectPath = project.projectPath
        val iter = projectPath.listIterator(projectPath.size)

        while (iter.hasPrevious()) {
            val currentProject = iter.previous()

            try {
                val scripts = getScriptNamesForProject(currentProject)
                scripts.removeAll(foundNames)
                foundNames.addAll(scripts)
                if (scripts.size > 0) {
                    result[currentProject] = scripts
                }
            } catch (e: IOException) {
                log.error(e.message)
                result.clear()
            }
        }
        return result
    }

    private val filter: KFunction1<File, Boolean> = File::isFile

    private fun getScriptNamesForProject(project: SProject): MutableList<String> {
        val scripts = mutableListOf<String>()
        val files = getPluginDataDirectory(project).listFiles(filter)
        if (files != null && files.isNotEmpty()) {
            for (file in files) {
                scripts.add(file.name)
            }
        }
        return scripts
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

    override fun saveScript(project: SProject, name: String, content: String) {
        val file = File(getPluginDataDirectory(project), name)
        val exists = file.exists()
        file.writeText(content)
        val message = "Gradle init script $name was ${if (exists) "updated" else "uploaded"}"
        configChangesListener.onPersist(project, file, configActionFactory.createAction(project, message))
    }

    override fun deleteScript(project: SProject, name: String): Boolean {
        var result = false
        try {
            val file = File(getPluginDataDirectory(project), name)
            result = FileUtil.delete(file)
            if (result) {
                val message = "Gradle init script $name was deleted"
                configChangesListener.onDelete(project, file, configActionFactory.createAction(project, message))
            }
        }
        catch (e: IOException) {
            log.error(e.message)
        }
        return result
    }

    private fun getPluginDataDirectory(project: SProject): File {
        return FileUtil.createDir(project.getPluginDataDirectory(pluginName))
    }
}
