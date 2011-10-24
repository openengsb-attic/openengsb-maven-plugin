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
 * guides through the creation of a domain for the OpenEngSB via the domain archetype
 *
 * @goal genDomain
 * @inheritedByDefault false
 * @requiresProject false
 * @aggregator true
 */
public class GenDomain extends MavenExecutorMojo {

    private boolean archetypeCatalogLocalOnly = false;

    // INPUTS
    private String archetypeVersion;

    private String domainName;
    private String version;
    private String projectName;
    private String groupId;
    private String artifactId;

    // CONSTANTS
    private static final String ARCHETYPE_GROUPID = "org.openengsb.tooling.archetypes";
    private static final String ARCHETYPE_ARTIFACTID = "org.openengsb.tooling.archetypes.domain";

    private static final String DOMAIN_GROUPIDPREFIX = "org.openengsb.domain.";

    private static final String DOMAIN_ARTIFACTIDPREFIX = "org.openengsb.domain.";

    private static final String DEFAULT_DOMAIN = "mydomain";

    private static final String DEFAULT_DOMAINNAME_PREFIX = "OpenEngSB :: Domain :: ";

    // DYNAMIC DEFAULTS
    private static final String DEFAULT_VERSION = "1.0.0-SNAPSHOT";

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
        MavenExecutor genDomainExecutor = getNewMavenExecutor(this);
        initializeMavenExecutionProperties(genDomainExecutor);
        genDomainExecutor.setRecursive(true);
        addMavenExecutor(genDomainExecutor);
    }

    @Override
    protected void validateIfExecutionIsAllowed() throws MojoExecutionException {
        throwErrorIfWrapperRequestIsRecursive();
    }

    @Override
    protected void postExec() throws MojoExecutionException {
        Tools.renameArtifactFolderAndUpdateParentPom(artifactId, domainName);
        System.out.println("DON'T FORGET TO ADD THE DOMAIN TO YOUR RELEASE/ASSEMBLY PROJECT!");
    }


    private void readUserInput() {
        Scanner sc = new Scanner(System.in);

        System.out.print("Use only local archetypeCatalog? (y/n): ");
        String in = sc.nextLine();
        if (in.equalsIgnoreCase("y")) {
            archetypeCatalogLocalOnly = true;
        }
        archetypeVersion = Tools.readValueFromStdin(sc, "Archetype Version", archetypeVersion);
        domainName = Tools.readValueFromStdin(sc, "Domain Name", DEFAULT_DOMAIN);
        version = Tools.readValueFromStdin(sc, "Domain Version", DEFAULT_VERSION);
        projectName = Tools.readValueFromStdin(sc, "Prefix for project names",
            String.format("%s%s", DEFAULT_DOMAINNAME_PREFIX, Tools.capitalizeFirst(domainName)));

        groupId = String.format("%s%s", DOMAIN_GROUPIDPREFIX, domainName);
        artifactId = String.format("%s%s", DOMAIN_ARTIFACTIDPREFIX, domainName);
    }

    private void initializeMavenExecutionProperties(MavenExecutor executor) {
        List<String> goals = Arrays.asList("archetype:generate");

        Properties userProperties = new Properties();

        userProperties.put("archetypeGroupId", ARCHETYPE_GROUPID);
        userProperties.put("archetypeArtifactId", ARCHETYPE_ARTIFACTID);
        userProperties.put("archetypeVersion", archetypeVersion);
        userProperties.put("groupId", groupId);
        userProperties.put("artifactId", artifactId);
        userProperties.put("version", version);
        userProperties.put("openengsbVersion", archetypeVersion);
        userProperties.put("domainName", domainName);
        userProperties.put("package", groupId);
        userProperties.put("name", projectName);
        userProperties.put("domainInterface", String.format("%s%s", Tools.capitalizeFirst(domainName), "Domain"));

        // local archetype catalog only
        if (archetypeCatalogLocalOnly) {
            userProperties.put("archetypeCatalog", "local");
        }

        executor.addGoals(goals);
        executor.addUserProperties(userProperties);
    }

}
