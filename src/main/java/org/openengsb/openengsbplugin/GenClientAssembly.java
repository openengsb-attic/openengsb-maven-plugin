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
 * @goal genAssembly
 * @inheritedByDefault false
 * @requiresProject true
 * @aggregator true
 */
public class GenClientAssembly extends MavenExecutorMojo {

    private boolean archetypeCatalogLocalOnly = false;

    // INPUT
    private String clientProjectGroupId;
    private String artifactId;
    private String clientProjectName;
    private String clientProjectVersion;
    private String clientProjectDescription;
    private String clientProjectUrl;
    private String scmConnection;
    private String scmDeveloperConnection;
    private String scmUrl;
    private String version;
    private String name;
    private String url;


    // CONSTANTS
    private static final String ARCHETYPE_GROUPID = "org.openengsb.tooling.archetypes.clientproject";
    private static final String ARCHETYPE_ARTIFACTID = "org.openengsb.tooling.archetypes.clientproject.assembly";

    private static final String PROJECT_GROUPID = "org.openengsb.client-project";
    private static final String PROJECT_ARTIFACTID = "openengsb-client-project-parent";

    private static final String PROJECT_NAME = "Client-Poject";
    private static final String PROJECT_VERSION = "1.0.0-SNAPSHOT";
    private static final String PROJECT_DESCRIPTION  = "Creates binary distribution and prepares configuration files ";
    private static final String PROJECT_URL = "http://www.openenbsb.org";

    private static final String SCM_CONNETION = "scm:git:git://github.com/clientproject";
    private static final String SCM_DEVELOPER_CONNETION = "scm:git:git@github.com:clientproject";
    private static final String SCM_URL = "http://github.com/clientproject";

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
        Tools.renameArtifactFolderAndUpdateParentPom(artifactId, "assembly");
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

        clientProjectGroupId = Tools.readValueFromStdin(sc, "Project Group Id", PROJECT_GROUPID);
        artifactId = Tools.readValueFromStdin(sc, "Project Artifact Id", PROJECT_ARTIFACTID);

        name = Tools.readValueFromStdin(sc, "Project Name", PROJECT_NAME);
        clientProjectName = name;
        clientProjectVersion = Tools.readValueFromStdin(sc, "Project Version", PROJECT_VERSION);
        clientProjectDescription = Tools.readValueFromStdin(sc, "Project Description", PROJECT_DESCRIPTION);
        clientProjectUrl = Tools.readValueFromStdin(sc, "Project Url", PROJECT_URL);
        version = Tools.readValueFromStdin(sc, "Version", defaultVersion);
        scmConnection = Tools.readValueFromStdin(sc, "SCM connection", SCM_CONNETION);
        scmDeveloperConnection = Tools.readValueFromStdin(sc, "SCM developer connection", SCM_DEVELOPER_CONNETION);
        scmUrl = Tools.readValueFromStdin(sc, "SCM Url", SCM_URL);
        url = Tools.readValueFromStdin(sc, "Project Url", "");

    }

    private void initializeMavenExecutionProperties(MavenExecutor executor) {
        List<String> goals = Arrays.asList(new String[]{"archetype:generate"});

        Properties userProperties = new Properties();

        userProperties.put("archetypeGroupId", ARCHETYPE_GROUPID);
        userProperties.put("archetypeArtifactId", ARCHETYPE_ARTIFACTID);
        userProperties.put("archetypeVersion", version);
        userProperties.put("artifactId", artifactId);
        userProperties.put("groupId", clientProjectGroupId);
        userProperties.put("version", version);
        userProperties.put("name", name);
        userProperties.put("clientProjectName", clientProjectName);
        userProperties.put("url", url);
        userProperties.put("clientProjectDescription", clientProjectDescription);
        userProperties.put("clientProjectUrl", clientProjectUrl);
        userProperties.put("parentversion", clientProjectVersion);
        userProperties.put("scmConnection", scmConnection);
        userProperties.put("scmDeveloperConnection", scmDeveloperConnection);
        userProperties.put("scmUrl", scmUrl);

        // local archetype catalog only
        if (archetypeCatalogLocalOnly) {
            userProperties.put("archetypeCatalog", "local");
        }

        executor.addGoals(goals);
        executor.addUserProperties(userProperties);
    }

}
