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

package teamcity.gradle.scripts.server;

import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.FileUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static teamcity.gradle.scripts.common.GradleInitScriptsPlugin.PLUGIN_NAME;

public class DefaultGradleScriptsManager implements GradleScriptsManager {

    @NotNull
    private static final Logger LOG = Logger.getLogger("jetbrains.buildServer.SERVER");

    @Override
    @NotNull
    public List<String> getScriptNames(@NotNull SProject project) {
        final List<String> result = new ArrayList<>();
        FileFilter filter = File::isFile;

        try {
            File pluginDataDirectory = getPluginDataDirectory(project);
            File[] files = pluginDataDirectory.listFiles(filter);
            if (files != null && files.length > 0) {
                for (File file : files) {
                    result.add(file.getName());
                }
            }
        } catch (IOException e) {
            LOG.error(e.getMessage());
            result.clear();
        }
        return result;
    }

    @Override
    @Nullable
    public String findScript(@NotNull SProject project, @NotNull String name) {
        try {
            File file = new File(getPluginDataDirectory(project), name);
            if (file.exists()) {
                return FileUtil.readText(file, "UTF-8");
            }
        }
        catch (Exception e) {
            ExceptionUtil.rethrowAsRuntimeException(e);
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

    private File getPluginDataDirectory(@NotNull SProject project) throws IOException {
        return FileUtil.createDir(project.getPluginDataDirectory(PLUGIN_NAME));
    }
}
