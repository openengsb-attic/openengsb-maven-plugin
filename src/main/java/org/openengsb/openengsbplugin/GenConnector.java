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
import org.openengsb.openengsbplugin.exceptions.NoVersionFoundException;
import org.openengsb.openengsbplugin.tools.MavenExecutor;
import org.openengsb.openengsbplugin.tools.OpenEngSBVersionResolver;
import org.openengsb.openengsbplugin.tools.Tools;

/**
 * guides through the creation of a connector for the OpenEngSB via the connector archetype
 *
 * @goal genConnector
 * @inheritedByDefault false
 * @requiresProject false
 * @aggregator true
 */
public class GenConnector extends MavenExecutorMojo {

    private boolean archetypeCatalogLocalOnly = false;

    // INPUT
    private String domainName;
    private String domainVersion;
    private String domainRange;
    private String domaininterface;
    private String connector;
    private String projectName;
    private String domainGroupId;
    private String domainArtifactId;
    private String scmConnection;
    private String scmDeveloperConnection;
    private String scmUrl;
    private String archetypeVersion;

    // CONSTANTS
    private static final String ARCHETYPE_GROUPID = "org.openengsb.tooling.archetypes";
    private static final String ARCHETYPE_ARTIFACTID = "org.openengsb.tooling.archetypes.connector";

    private static final String DOMAIN_GROUPIDPREFIX = "org.openengsb.domain.";

    private static final String DOMAIN_ARTIFACTIDPREFIX = "openengsb-domain-";

    private static final String CONNECTOR_GROUPID = "org.openengsb.connector";

    private static final String DEFAULT_DOMAIN = "domain";
    private static final String DEFAULT_DOMAIN_VERSION = "1.3.0-SNAPSHOT";
    private static final String DEFAULT_DOMAIN_RANGE = "[1.2,1.3)";
    private static final String SCM_CONNECTION = "scm:git:git://github"
        + ".com/openengsb/openengsb-connector-connectorName.git";
    private static final String SCM_DEVELOPER_CONNECTION =
        "scm:git:git@github.com:openengsb/openengsb-connector-connectorName.git";
    private static final String SCM_URL = "http://github.com/openengsb/openengsb-connector-connectorName";

    @Override
    protected void configure() throws MojoExecutionException {
        OpenEngSBVersionResolver versionResolver = new OpenEngSBVersionResolver();
        try {
            archetypeVersion = versionResolver.getLatestVersion();
        } catch (NoVersionFoundException e) {
            System.err.println("#############################################################");
            System.err.println("AN ERROR OCCURED: " + e.getMessage());
            System.err.println("#############################################################");
            archetypeVersion = "";
        }
        readUserInput();
        MavenExecutor genConnectorExecutor = getNewMavenExecutor(this);
        initializeMavenExecutionProperties(genConnectorExecutor);
        genConnectorExecutor.setRecursive(true);
        addMavenExecutor(genConnectorExecutor);
    }

    @Override
    protected void validateIfExecutionIsAllowed() throws MojoExecutionException {
        throwErrorIfWrapperRequestIsRecursive();
    }

    @Override
    protected void postExec() throws MojoExecutionException {
        System.out.println("DON'T FORGET TO ADD THE CONNECTOR TO YOUR RELEASE/ASSEMBLY PROJECT!");
    }

    private void readUserInput() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Use only local archetypeCatalog? (y/n): ");
        String in = sc.nextLine();
        if (in.equalsIgnoreCase("y")) {
            archetypeCatalogLocalOnly = true;
        }

        archetypeVersion = Tools.readValueFromStdin(sc, "Archetype and OpenEngSB Version", archetypeVersion);
        domainName = Tools.readValueFromStdin(sc, "Domain Name", DEFAULT_DOMAIN);
        domainVersion = Tools.readValueFromStdin(sc, "Domain Version", DEFAULT_DOMAIN_VERSION);
        domainRange = Tools.readValueFromStdin(sc, "Domain Version Range", DEFAULT_DOMAIN_RANGE);
        domaininterface = Tools.readValueFromStdin(sc, "Domain Interface",
            String.format("%s%s", Tools.capitalizeFirst(domainName), "Domain"));
        connector = Tools.readValueFromStdin(sc, "Connector Name", "myconnector");

        projectName = Tools.readValueFromStdin(sc, "Project Name",
            String.format("%s", Tools.capitalizeFirst(connector)));
        domainGroupId = String.format("%s%s", DOMAIN_GROUPIDPREFIX, domainName);
        domainArtifactId = String.format("%s%s", DOMAIN_ARTIFACTIDPREFIX, domainName);
        scmConnection = Tools.readValueFromStdin(sc, "SCM Connection", SCM_CONNECTION);
        scmDeveloperConnection = Tools.readValueFromStdin(sc, "SCM Developer Connection", SCM_DEVELOPER_CONNECTION);
        scmUrl = Tools.readValueFromStdin(sc, "SCM Url", SCM_URL);
    }

    private void initializeMavenExecutionProperties(MavenExecutor executor) {
        List<String> goals = Arrays.asList("archetype:generate");

        Properties userProperties = new Properties();

        userProperties.put("archetypeGroupId", ARCHETYPE_GROUPID);
        userProperties.put("archetypeArtifactId", ARCHETYPE_ARTIFACTID);
        userProperties.put("archetypeVersion", archetypeVersion);

        userProperties.put("domainArtifactId", domainArtifactId);
        userProperties.put("groupId", CONNECTOR_GROUPID);
        userProperties.put("artifactId", String.format("%s.%s", CONNECTOR_GROUPID, connector.toLowerCase()));
        userProperties.put("connectorNameL", connector.toLowerCase());
        userProperties.put("groupId", CONNECTOR_GROUPID);
        userProperties.put("version", archetypeVersion);
        userProperties.put("domainInterface", domaininterface);
        userProperties.put("domain", String.format("%s%s", domainName, "Domain"));
        userProperties.put("domainL", domainName.toLowerCase());
        userProperties.put("domainVersion", domainVersion);
        userProperties.put("domainRange", domainRange);
        userProperties.put("domainName", domainName);
        userProperties.put("package", String.format("%s.%s", CONNECTOR_GROUPID, connector.toLowerCase()));
        userProperties.put("domainPackage", domainGroupId);
        userProperties.put("name", projectName);
        userProperties.put("connectorName", Tools.capitalizeFirst(connector));
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
