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

package com.github.rodm.teamcity.gradle.scripts.server;

import jetbrains.buildServer.serverSide.CopiedObjects;
import jetbrains.buildServer.serverSide.CustomSettingsMapper;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.FileUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.*;

import static jetbrains.buildServer.log.Loggers.SERVER_CATEGORY;
import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.PLUGIN_NAME;

public class DefaultGradleScriptsManager implements GradleScriptsManager, CustomSettingsMapper {

    @NotNull
    private static final Logger LOG = Logger.getLogger(SERVER_CATEGORY + ".GradleInitScripts");

    @Override
    public void mapData(@NotNull final CopiedObjects copiedObjects) {
        for (Map.Entry<SProject, SProject> entry: copiedObjects.getCopiedProjectsMap().entrySet()) {
            SProject source = entry.getKey();
            SProject target = entry.getValue();
            File sourceDir = source.getPluginDataDirectory(PLUGIN_NAME);
            File[] files = sourceDir.listFiles();
            if (files != null && files.length > 0) {
                File targetDir;
                try {
                    targetDir = FileUtil.createDir(target.getPluginDataDirectory(PLUGIN_NAME));
                } catch (IOException e) {
                    LOG.warn("Could not create directory for project Gradle init scripts", e);
                    continue;
                }
                for (File sourceFile : files) {
                    final File targetFile = new File(targetDir, sourceFile.getName());
                    try {
                        FileUtil.copy(sourceFile, targetFile);
                    } catch (IOException e) {
                        LOG.warn("Could not copy script file: " + sourceFile.getAbsolutePath() + " to: " + targetFile.getAbsolutePath(), e);
                    }
                }
            }
        }
    }

    @Override
    @NotNull
    public Map<SProject, List<String>> getScriptNames(@NotNull SProject project) {
        final Set<String> foundNames = new HashSet<>();
        final Map<SProject, List<String>> result = new LinkedHashMap<>();
        FileFilter filter = File::isFile;

        List<SProject> projectPath = project.getProjectPath();
        ListIterator<SProject> iter = projectPath.listIterator(projectPath.size());

        while (iter.hasPrevious()) {
            SProject currentProject = iter.previous();

            try {
                File pluginDataDirectory = getPluginDataDirectory(currentProject);
                File[] files = pluginDataDirectory.listFiles(filter);
                if (files != null && files.length > 0) {
                    List<String> scripts = new ArrayList<>();
                    for (File file : files) {
                        if (!foundNames.contains(file.getName())) {
                            scripts.add(file.getName());
                            foundNames.add(file.getName());
                        }
                    }
                    if (scripts.size() > 0) {
                        result.put(currentProject, scripts);
                    }
                }
            } catch (IOException e) {
                LOG.error(e.getMessage());
                result.clear();
            }
        }
        return result;
    }

    @Override
    public int getScriptsCount(@NotNull SProject project) {
        return getScriptNames(project).values().stream().mapToInt(List::size).sum();
    }

    @Override
    @Nullable
    public String findScript(@NotNull SProject project, @NotNull String name) {
        List<SProject> projectPath = project.getProjectPath();
        ListIterator<SProject> iter = projectPath.listIterator(projectPath.size());

        while (iter.hasPrevious()) {
            SProject currentProject = iter.previous();

            try {
                File file = new File(getPluginDataDirectory(currentProject), name);
                if (file.exists()) {
                    return FileUtil.readText(file, "UTF-8");
                }
            } catch (Exception e) {
                ExceptionUtil.rethrowAsRuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public boolean deleteScript(@NotNull SProject project, @NotNull String name) {
        boolean result = false;
        try {
            File file = new File(getPluginDataDirectory(project), name);
            result = FileUtil.delete(file);
        }
        catch (IOException e) {
            LOG.error(e.getMessage());
        }
        return result;
    }

    @NotNull
    private File getPluginDataDirectory(@NotNull SProject project) throws IOException {
        return FileUtil.createDir(project.getPluginDataDirectory(PLUGIN_NAME));
    }
}
