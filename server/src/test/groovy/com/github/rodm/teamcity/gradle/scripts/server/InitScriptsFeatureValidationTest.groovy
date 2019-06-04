/*
 * Copyright 2019 Rod MacKenzie.
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

import jetbrains.buildServer.web.openapi.PluginDescriptor
import org.junit.Before
import org.junit.Test

import static com.github.rodm.teamcity.gradle.scripts.GradleInitScriptsPlugin.INIT_SCRIPT_NAME
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.hasSize
import static org.hamcrest.Matchers.is
import static org.hamcrest.Matchers.not
import static org.hamcrest.Matchers.nullValue
import static org.mockito.Mockito.mock

class InitScriptsFeatureValidationTest {

    private PluginDescriptor descriptor

    @Before
    void setup() {
        descriptor = mock(PluginDescriptor)
    }

    @Test
    void 'build feature should return a parameters processor'() {
        def buildFeature = new InitScriptsBuildFeature(descriptor)

        assertThat(buildFeature.getParametersProcessor(), not(nullValue()))
    }

    @Test
    void 'parameters processor should return invalid property when init script not set'() {
        def processor = new InitScriptsParametersProcessor()

        def invalidProperties = processor.process([:])

        assertThat(invalidProperties, hasSize(1))
        def invalidProperty = invalidProperties[0]
        assertThat(invalidProperty.propertyName, is(equalTo(INIT_SCRIPT_NAME)) )
        assertThat(invalidProperty.invalidReason, is(equalTo('An init script must be selected')) )
    }

    @Test
    void 'parameters processor should return empty collection when init script is set'() {
        def processor = new InitScriptsParametersProcessor()

        def properties = [(INIT_SCRIPT_NAME): 'init.gradle']
        def invalidProperties = processor.process(properties)

        assertThat(invalidProperties, hasSize(0))
    }
}
