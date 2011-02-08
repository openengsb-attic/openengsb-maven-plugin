/**
 * Copyright 2010 OpenEngSB Division, Vienna University of Technology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openengsb.openengsbplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.base.ConfiguredMojo;
import org.openengsb.openengsbplugin.tools.MavenExecutor;

/**
 * equivalent to
 * <code>mvn install -Prelease,nightly -Dmaven.test.skip=true</code>
 * 
 * @goal assemble
 * 
 * @inheritedByDefault false
 * 
 * @requiresProject true
 * 
 * @aggregator true
 * 
 */
public class Assemble extends ConfiguredMojo {

    public Assemble() {
        configPath = "assembleMojo/assembleConfig.xml";
        configProfileXpath = "/am:assembleMojo/am:profile";
    }

    @Override
    protected void validateIfExecutionIsAllowed() throws MojoExecutionException {
        throwErrorIfWrapperRequestIsRecursive();
        throwErrorIfProjectIsNotExecutedInRootDirectory();
    }

    @Override
    protected void configureCoCMojo() throws MojoExecutionException {
        List<String> goals = new ArrayList<String>();
        goals.add("install");
        Properties userProperties = new Properties();
        userProperties.put("maven.test.skip", "true");

        MavenExecutor assembleMojoExecutor = getNewMavenExecutor(this);
        assembleMojoExecutor.addGoals(goals);
        assembleMojoExecutor.addUserProperties(userProperties);

        assembleMojoExecutor.setRecursive(true);
        assembleMojoExecutor.setCustomPomFile(cocPom);
        assembleMojoExecutor.addActivatedProfiles(Arrays.asList(new String[] { cocProfile }));

        addMavenExecutor(assembleMojoExecutor);
    }

}
