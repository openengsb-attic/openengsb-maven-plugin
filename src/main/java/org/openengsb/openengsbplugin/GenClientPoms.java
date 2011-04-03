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

import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.base.MavenExecutorMojo;
import org.openengsb.openengsbplugin.tools.MavenExecutor;
import org.openengsb.openengsbplugin.tools.Tools;

/**
 * guides through the creation of a connector for the OpenEngSB via the
 * connector archetype
 *
 * @goal genPoms
 * @inheritedByDefault false
 * @requiresProject true
 * @aggregator true
 */
public class GenClientPoms extends MavenExecutorMojo {

    private boolean archetypeCatalogLocalOnly = false;

    private String artifactId;
    // INPUT
    private String groupId;
    private String clientProjectVersion;
    private String version;
    private String name;


    // CONSTANTS
    private static final String ARCHETYPE_GROUPID = "org.openengsb.tooling.archetypes.clientproject";
    private static final String ARCHETYPE_ARTIFACTID = "org.openengsb.tooling.archetypes.clientproject.poms";

    private static final String PROJECT_GROUPID = "org.openengsb.client-project";
    private static final String PROJECT_ARTIFACTID = "openengsb-client-project-parent";

    private static final String PROJECT_NAME = "Client-Poject ";
    private static final String PROJECT_VERSION = "1.0.0-SNAPSHOT";

    // DYNAMIC DEFAULTS

    private String defaultVersion;

    @Override
    protected void configure() throws MojoExecutionException {
        initDefaults();
        readUserInput();
        MavenExecutor genConnectorExecutor = getNewMavenExecutor(this);
        initializeMavenExecutionProperties(genConnectorExecutor);
        genConnectorExecutor.setRecursive(true);
        addMavenExecutor(genConnectorExecutor);
    }

    protected void validateIfExecutionIsAllowed() throws MojoExecutionException {
        throwErrorIfWrapperRequestIsRecursive();
    }

    @Override
    protected void postExec() throws MojoExecutionException {
        Tools.renameArtifactFolderAndUpdateParentPom(artifactId, "poms");
        System.out.println("DON'T FORGET TO ADD THE CONNECTOR TO YOUR RELEASE/ASSEMBLY PROJECT!");
    }

    private void initDefaults() {
        // version should be the same as the version of the OpenEngSB
        defaultVersion = getProject().getVersion();
    }

    private void readUserInput() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Use only local archetypeCatalog? (y/n): ");
        String in = sc.nextLine();
        if (in.equalsIgnoreCase("y")) {
            archetypeCatalogLocalOnly = true;
        }

        groupId = Tools.readValueFromStdin(sc, "Project Group Id", PROJECT_GROUPID);
        artifactId = Tools.readValueFromStdin(sc, "Project Artifact Id", PROJECT_ARTIFACTID);
        name = Tools.readValueFromStdin(sc, "Project Name", PROJECT_NAME);
        clientProjectVersion = Tools.readValueFromStdin(sc, "Project Version", PROJECT_VERSION);
        version = Tools.readValueFromStdin(sc, "Version", defaultVersion);

    }

    private void initializeMavenExecutionProperties(MavenExecutor executor) {
        List<String> goals = Arrays.asList(new String[]{"archetype:generate"});

        Properties userProperties = new Properties();

        userProperties.put("archetypeGroupId", ARCHETYPE_GROUPID);
        userProperties.put("archetypeArtifactId", ARCHETYPE_ARTIFACTID);
        userProperties.put("archetypeVersion", version);
        userProperties.put("artifactId", artifactId);
        userProperties.put("groupId", groupId);
        userProperties.put("version", version);
        userProperties.put("name", name);
        userProperties.put("parentversion", clientProjectVersion);

        // local archetype catalog only
        if (archetypeCatalogLocalOnly) {
            userProperties.put("archetypeCatalog", "local");
        }

        executor.addGoals(goals);
        executor.addUserProperties(userProperties);
    }

}
