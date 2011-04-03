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
 * guides through the creation of a new client project for the OpenEngSB via the
 * client creation archetype
 *
 * @goal genClientProjectRoot
 * @inheritedByDefault false
 * @requiresProject false
 * @aggregator false
 */
public class GenClientRoot extends MavenExecutorMojo {

    private boolean archetypeCatalogLocalOnly = false;

    // INPUT
    private String clientProjectGroupId;
    private String artifactId;
    private String clientProjectName;
    private String clientProjectVersion;
    private String clientProjectDescription;
    private String clientProjectUrl;
    private String openengsbVersion;
    private String openengsbMavenPluginVersion;
    private String pluginAssemblyVersion;

    // CONSTANTS
    private static final String ARCHETYPE_GROUPID = "org.openengsb.tooling.archetypes.clientproject";
    private static final String ARCHETYPE_ARTIFACTID = "org.openengsb.tooling.archetypes.clientproject.root";

    private static final String PROJECT_GROUPID = "org.openengsb.client-project";
    private static final String PROJECT_ARTIFACTID = "openengsb-client-project-parent";

    private static final String PROJECT_NAME = "Client-Poject :: Parent";
    private static final String PROJECT_VERSION = "1.0.0-SNAPSHOT";
    private static final String PROJECT_DESCRIPTION = "This is a client project for the OpenEngSB";
    private static final String PROJECT_URL = "http://www.openenbsb.org";

    private static final String OPENENGSB_MAVEN_VERSION = "1.4.0-SNAPSHOT";
    private static final String OPENENGSB_VERSION = "1.2.0-SNAPSHOT";
    private static final String PLUGIN_ASSEMBLY_VERSION = "2.2-beta-5";


    @Override
    protected void configure() throws MojoExecutionException {
        readUserInput();
        MavenExecutor genClientProjectExecutor = getNewMavenExecutor(this);
        initializeMavenExecutionProperties(genClientProjectExecutor);
        genClientProjectExecutor.setRecursive(true);
        addMavenExecutor(genClientProjectExecutor);
    }

    protected void validateIfExecutionIsAllowed() throws MojoExecutionException {
        throwErrorIfWrapperRequestIsRecursive();
    }

    @Override
    protected void postExec() throws MojoExecutionException {
        System.out.println("Your client project was created successfully");
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

        clientProjectName = Tools.readValueFromStdin(sc, "Project Name", PROJECT_NAME);
        clientProjectVersion = Tools.readValueFromStdin(sc, "Project Version", PROJECT_VERSION);
        clientProjectDescription = Tools.readValueFromStdin(sc, "Project Description", PROJECT_DESCRIPTION);
        clientProjectUrl = Tools.readValueFromStdin(sc, "Project Url", PROJECT_URL);

        openengsbVersion = Tools.readValueFromStdin(sc, "OpenEngSB version", OPENENGSB_VERSION);
        openengsbMavenPluginVersion = Tools
            .readValueFromStdin(sc, "OpenEngSB maven plugin Version", OPENENGSB_MAVEN_VERSION);
        pluginAssemblyVersion = Tools.readValueFromStdin(sc, "Project Artifact Id", PLUGIN_ASSEMBLY_VERSION);

    }

    private void initializeMavenExecutionProperties(MavenExecutor executor) {
        List<String> goals = Arrays.asList(new String[]{"archetype:generate"});
        Properties userProperties = new Properties();
        System.out.print("12 ");

        userProperties.put("archetypeGroupId", ARCHETYPE_GROUPID);
        System.out.print("14 ");
        userProperties.put("archetypeArtifactId", ARCHETYPE_ARTIFACTID);
        System.out.print("14 ");
        userProperties.put("archetypeVersion", clientProjectVersion);
        System.out.print("14 ");
        userProperties.put("artifactId", artifactId);
        System.out.print("14 ");
        userProperties.put("groupId", clientProjectGroupId);
        System.out.print("14 ");
        userProperties.put("version", clientProjectVersion);
        userProperties.put("name", clientProjectName);
        userProperties.put("clientProjectDescription", clientProjectDescription);
        userProperties.put("clientProjectUrl", clientProjectUrl);
        userProperties.put("openengsbVersion", openengsbVersion);
        userProperties.put("openengsbMavenPluginVersion", openengsbMavenPluginVersion);
        userProperties.put("pluginAssemblyVersion", pluginAssemblyVersion);

        // local archetype catalog only
        if (archetypeCatalogLocalOnly) {
            userProperties.put("archetypeCatalog", "local");
        }

        executor.addGoals(goals);
        executor.addUserProperties(userProperties);
    }

}
