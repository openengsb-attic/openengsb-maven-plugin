/**
 * Licensed to the Austrian Association for Software Tool Integration (AASTI)
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. The AASTI licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.openengsbplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.base.ConfiguredMojo;
import org.openengsb.openengsbplugin.tools.MavenExecutor;

/**
 * Mojo to perform nightly releases. This mojo activates the "nightly" profile
 * in the project, where you can put your configuration for nightly releases.
 * 
 * @goal releaseNightly
 * 
 * @inheritedByDefault false
 * 
 * @requiresProject true
 * 
 * @aggregator true
 */
public class ReleaseNightly extends ConfiguredMojo {

    public ReleaseNightly() {
        configs.add("release/nightlyConfig.xml");
    }

    @Override
    protected void configureCoCMojo() throws MojoExecutionException {
        List<String> goals = new ArrayList<String>();
        goals.add("clean");
        goals.add("install");
        goals.add("deploy");

        List<String> activatedProfiles = new ArrayList<String>();
        activatedProfiles.add(cocProfile);
        activatedProfiles.add("release");
        activatedProfiles.add("nightly");

        Properties userProperties = new Properties();
        userProperties.put("maven.test.skip", "true");

        MavenExecutor releaseNightlyExecutor = getNewMavenExecutor(this);
        releaseNightlyExecutor.addGoals(goals);
        releaseNightlyExecutor.addActivatedProfiles(activatedProfiles);
        releaseNightlyExecutor.addUserProperties(userProperties);

        releaseNightlyExecutor.setRecursive(true);

        addMavenExecutor(releaseNightlyExecutor);
    }

    @Override
    protected void validateIfExecutionIsAllowed() throws MojoExecutionException {
        throwErrorIfProjectIsNotExecutedInRootDirectory();
    }

}
