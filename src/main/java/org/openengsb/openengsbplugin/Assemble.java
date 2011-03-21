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
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.base.ConfiguredMojo;
import org.openengsb.openengsbplugin.tools.MavenExecutor;

/**
 * 
 * Installs the OpenEngSB and skips tests. Furthermore a nightly profile is
 * activated if available in your poms.
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
        pomConfigs.put("pom.xml", Arrays.asList(new String[] { "assemble/assembleConfig.xml" }));
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
        assembleMojoExecutor.addActivatedProfiles(Arrays.asList(new String[] { "nightly", cocProfile }));

        addMavenExecutor(assembleMojoExecutor);
    }

}
