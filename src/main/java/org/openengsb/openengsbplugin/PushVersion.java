/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.openengsbplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.base.MavenExecutorMojo;
import org.openengsb.openengsbplugin.tools.MavenExecutor;

/**
 * update development version
 * 
 * @goal pushVersion
 * 
 * @inheritedByDefault false
 * 
 * @requiresProject true
 * 
 * @aggregator true
 * 
 */
public class PushVersion extends MavenExecutorMojo {

    /**
     * the new version
     * 
     * @parameter expression="${developmentVersion}"
     * 
     * @required
     */
    private String developmentVersion;

    protected void validateIfExecutionIsAllowed() throws MojoExecutionException {
        throwErrorIfWrapperRequestIsRecursive();
        throwErrorIfProjectIsNotExecutedInRootDirectory();
    }

    protected void configure() {
        List<String> goals = new ArrayList<String>();
        goals.add("release:update-versions");

        Properties userProperties = new Properties();
        userProperties.put("autoVersionSubmodules", "true");
        userProperties.put("developmentVersion", developmentVersion);

        MavenExecutor pushVersionExecutor = getNewMavenExecutor(this);
        pushVersionExecutor.addGoals(goals);
        pushVersionExecutor.addUserProperties(userProperties);

        pushVersionExecutor.setRecursive(true);
        pushVersionExecutor.setInterActiveMode(false);

        addMavenExecutor(pushVersionExecutor);
    }

}
