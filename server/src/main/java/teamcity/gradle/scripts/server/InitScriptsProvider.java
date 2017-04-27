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

import jetbrains.buildServer.serverSide.*;
import org.apache.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static teamcity.gradle.scripts.common.GradleInitScriptsPlugin.FEATURE_TYPE;
import static teamcity.gradle.scripts.common.GradleInitScriptsPlugin.INIT_SCRIPT_CONTENT;
import static teamcity.gradle.scripts.common.GradleInitScriptsPlugin.INIT_SCRIPT_NAME;

public class InitScriptsProvider implements BuildStartContextProcessor {

    @NotNull
    private static final Logger LOG = Logger.getLogger("jetbrains.buildServer.SERVER");

    @NotNull
    private final GradleScriptsManager scriptsManager;

    public InitScriptsProvider(@NotNull GradleScriptsManager scriptsManager) {
        this.scriptsManager = scriptsManager;
    }

    @Override
    public void updateParameters(@NotNull BuildStartContext context) {
        for (SRunnerContext runnerContext : context.getRunnerContexts()) {
            if (isGradleRunner(runnerContext)) {
                SBuildType buildType = context.getBuild().getBuildType();
                if (buildType != null) {
                    Collection<SBuildFeatureDescriptor> features = buildType.getBuildFeaturesOfType(FEATURE_TYPE);
                    for (SBuildFeatureDescriptor feature : features) {

                        SProject project = buildType.getProject();
                        String scriptName = feature.getParameters().get(INIT_SCRIPT_NAME);
                        String scriptContent = scriptsManager.findScript(project, scriptName);
                        if (scriptContent != null) {
                            runnerContext.addRunnerParameter(INIT_SCRIPT_NAME, scriptName);
                            runnerContext.addRunnerParameter(INIT_SCRIPT_CONTENT, scriptContent);
                        } else {
                            LOG.error("Init script content for '" + scriptName + "' is empty");
                        }
                    }
                }
            }
        }
    }

    boolean isGradleRunner(SRunnerContext runnerContext) {
        return "gradle-runner".equals(runnerContext.getRunType().getType());
    }
}
