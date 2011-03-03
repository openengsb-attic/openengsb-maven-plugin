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

package org.openengsb.openengsbplugin.base;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.tools.MavenExecutor;
import org.openengsb.openengsbplugin.tools.Tools;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public abstract class LicenseMojo extends ConfiguredMojo {
    
    private static final Logger LOG = Logger.getLogger(LicenseMojo.class);

    // #################################
    // set these in subclass constructor
    // #################################

    protected String mavenLicensePluginGoal;

    // #################################

    private static final String WRAPPED_GOAL = "validate";
    private static final String HEADER_PATH = "license/header.txt";
    private File licenseHeaderFile;

    public LicenseMojo() {
        configs.add("license/licenseConfig.xml");
    }

    @Override
    protected final void configureCoCMojo() throws MojoExecutionException {
        List<String> goals = new ArrayList<String>();
        goals.add(WRAPPED_GOAL);

        MavenExecutor licenseMojoExecutor = getNewMavenExecutor(this);
        licenseMojoExecutor.addGoals(goals);

        licenseMojoExecutor.setRecursive(true);
        licenseMojoExecutor.addActivatedProfiles(Arrays.asList(new String[] { cocProfile }));

        addMavenExecutor(licenseMojoExecutor);
    }

    @Override
    protected final void validateIfExecutionIsAllowed() throws MojoExecutionException {
    }

    public static File readHeaderStringAndwriteHeaderIntoTmpFile() throws MojoExecutionException {
        try {
            String headerString = IOUtils.toString(LicenseMojo.class.getClassLoader().getResourceAsStream(HEADER_PATH));
            File generatedFile = Tools.generateTmpFile(headerString, ".txt");
            return generatedFile;
        } catch (Exception e) {
            throw new MojoExecutionException("Couldn't create license header temp file!", e);
        }
    }

    @Override
    protected void modifyMojoConfiguration(Document configuredPom) throws MojoExecutionException {
        try {
            licenseHeaderFile = readHeaderStringAndwriteHeaderIntoTmpFile();
            FILES_TO_REMOVE_FINALLY.add(licenseHeaderFile);
            insertGoalAndSetHeaderPath(configuredPom, cocProfileXpath, mavenLicensePluginGoal,
                    licenseHeaderFile.getAbsolutePath());
        } catch (XPathExpressionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }

    public static void insertGoalAndSetHeaderPath(Document configuredPom, String profileXpath, String goalToInsert,
            String headerPathToInsert) throws XPathExpressionException {
        Node node = configuredPom.createElementNS(POM_NS_URI, "goal");
        node.setTextContent(goalToInsert);

        String licensePluginXPath = profileXpath + "/pom:build/pom:plugins/pom:plugin"
                + "[pom:groupId='com.mycila.maven-license-plugin' and pom:artifactId='maven-license-plugin']";

        Tools.insertDomNode(configuredPom, node, licensePluginXPath + "/pom:executions/pom:execution/pom:goals",
                NS_CONTEXT);

        LOG.trace(String.format("headerPath: \"%s\"", headerPathToInsert));
        
        node = configuredPom.createElementNS(POM_NS_URI, "header");
        node.setTextContent(headerPathToInsert);

        Tools.insertDomNode(configuredPom, node, licensePluginXPath + "/pom:configuration", NS_CONTEXT);
    }

}
