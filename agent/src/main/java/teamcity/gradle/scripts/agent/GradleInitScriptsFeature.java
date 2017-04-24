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

package teamcity.gradle.scripts.agent;

import jetbrains.buildServer.agent.*;
import jetbrains.buildServer.util.EventDispatcher;
import jetbrains.buildServer.util.FileUtil;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import static teamcity.gradle.scripts.common.GradleInitScriptsPlugin.FEATURE_TYPE;
import static teamcity.gradle.scripts.common.GradleInitScriptsPlugin.INIT_SCRIPT_CONTENT;
import static teamcity.gradle.scripts.common.GradleInitScriptsPlugin.INIT_SCRIPT_NAME;

public class GradleInitScriptsFeature extends AgentLifeCycleAdapter {

    private static Logger LOG = Logger.getLogger("jetbrains.buildServer.AGENT");

    private static String GRADLE_CMD_PARAMS = "ui.gradleRunner.additional.gradle.cmd.params";

    private File initScriptFile;

    public GradleInitScriptsFeature(EventDispatcher<AgentLifeCycleListener> eventDispatcher) {
        eventDispatcher.addListener(this);
    }

    @Override
    public void beforeRunnerStart(@NotNull BuildRunnerContext runner) {
        AgentRunningBuild build = runner.getBuild();
        Collection<AgentBuildFeature> features = build.getBuildFeaturesOfType(FEATURE_TYPE);

        if (!features.isEmpty()) {
            Map<String, String> runnerParameters = runner.getRunnerParameters();
            String initScriptName = runnerParameters.get(INIT_SCRIPT_NAME);
            String initScriptContent = runnerParameters.get(INIT_SCRIPT_CONTENT);

            if (initScriptContent == null) {
                throw new RuntimeException("Runner is configured to use init script '" + initScriptName + "', but no content was found. Please check runner settings.");
            }

            try {
                initScriptFile = FileUtil.createTempFile(build.getBuildTempDirectory(), "init_", ".gradle", true);
                FileUtil.writeFile(initScriptFile, initScriptContent, "UTF-8");

                String params = runnerParameters.getOrDefault(GRADLE_CMD_PARAMS, "");
                String initScriptParams = "--init-script " + initScriptFile.getAbsolutePath();
                runner.addRunnerParameter(GRADLE_CMD_PARAMS, initScriptParams + " " + params);
            } catch (IOException e) {
                LOG.info("Failed to write init script: " + e.getMessage());
            }
        }
    }

    @Override
    public void runnerFinished(@NotNull BuildRunnerContext runner, @NotNull BuildFinishedStatus status) {
        if (initScriptFile != null) {
            FileUtil.delete(initScriptFile);
            initScriptFile = null;
        }
    }
}
