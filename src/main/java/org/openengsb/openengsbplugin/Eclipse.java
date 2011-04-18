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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.base.ConfiguredMojo;
import org.openengsb.openengsbplugin.base.LicenseMojo;
import org.openengsb.openengsbplugin.tools.MavenExecutor;
import org.openengsb.openengsbplugin.tools.Tools;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Validates license headers.
 *
 * @goal eclipse
 *
 * @inheritedByDefault false
 *
 * @requiresProject true
 *
 * @aggregator true
 *
 */
public class Eclipse extends ConfiguredMojo {

    private static final Logger LOG = Logger.getLogger(Eclipse.class);

    private static final String CHECKSTYLE_CHECKER_CONFIG_PATH = "checkstyle/checkstyle.xml";
    private static final String CHECKSTYLE_ECLIPSE_CONFIG_PATH = "eclipse/eclipseCheckstyle.xml";

    private File checkstyleCheckerConfig;

    public Eclipse() {
        pomConfigs.put("pom.xml", Arrays.asList(new String[]{ "eclipse/eclipseConfig.xml" }));
    }

    @Override
    protected void configureCoCMojo() throws MojoExecutionException {
        checkstyleCheckerConfig = writeCheckstyleCheckerConfig();

        List<String> goals = new ArrayList<String>();
        goals.add("eclipse:eclipse");

        MavenExecutor eclipseMojoExecutor = getNewMavenExecutor(this);
        eclipseMojoExecutor.addGoals(goals);
        eclipseMojoExecutor.setRecursive(true);
        eclipseMojoExecutor.addActivatedProfiles(Arrays.asList(new String[]{ cocProfile }));

        addMavenExecutor(eclipseMojoExecutor);
    }

    @Override
    protected void validateIfExecutionIsAllowed() throws MojoExecutionException {
    }

    private File writeCheckstyleEclipseConfigAndSetCheckerConfigPath() throws MojoExecutionException {
        try {
            String checkstyleEclipseConfigContent = IOUtils.toString(LicenseMojo.class.getClassLoader()
                    .getResourceAsStream(CHECKSTYLE_ECLIPSE_CONFIG_PATH));
            Document configDocument = Tools.parseXMLFromString(checkstyleEclipseConfigContent, false);
            tryExtractLicenseHeader(configDocument);
            Node node = Tools.evaluateXPath("/fileset-config/local-check-config", configDocument, null,
                    XPathConstants.NODE, Node.class);
            LOG.trace(String.format("Found node: %s, # of attributes: %d", node, node.getAttributes().getLength()));
            node.getAttributes().getNamedItem("location").setTextContent(checkstyleCheckerConfig.getAbsolutePath());
            checkstyleEclipseConfigContent = Tools.serializeXML(configDocument);
            checkstyleEclipseConfigContent = addHeader(checkstyleEclipseConfigContent);
            return Tools.generateTmpFile(checkstyleEclipseConfigContent, ".xml");
        } catch (Exception e) {
            throw new MojoExecutionException("Couldn't create checkstyle checker config file!", e);
        }
    }

    private File writeCheckstyleCheckerConfig() throws MojoExecutionException {
        try {
            String checkerConfigContent = IOUtils.toString(LicenseMojo.class.getClassLoader().getResourceAsStream(
                    CHECKSTYLE_CHECKER_CONFIG_PATH));
            File checkerConfig = new File(".checkstyleCheckerConfig");
            FileUtils.writeStringToFile(checkerConfig, checkerConfigContent);
            return checkerConfig;
        } catch (Exception e) {
            throw new MojoExecutionException("Couldn't create checkstyle checker config file!", e);
        }
    }

    @Override
    protected void modifyMojoConfiguration(String pomPath, Document configuredPom) throws MojoExecutionException {
        if (pomPath.equals("pom.xml")) {
            try {
                File checkstyleEclipseConfig = writeCheckstyleEclipseConfigAndSetCheckerConfigPath();
                FILES_TO_REMOVE_FINALLY.add(checkstyleEclipseConfig);
                setCheckstyleEclipseConfigLocation(configuredPom, cocProfileXpath,
                        checkstyleEclipseConfig.getAbsolutePath());
            } catch (XPathExpressionException e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    private void setCheckstyleEclipseConfigLocation(Document configuredPom, String profileXpath,
            String locationToInsert) throws XPathExpressionException {
        Node node = configuredPom.createElementNS(POM_NS_URI, "location");
        node.setTextContent(locationToInsert);

        String eclipsePluginXPath = profileXpath + "/pom:build/pom:plugins/pom:plugin"
                + "[pom:groupId='org.apache.maven.plugins' and pom:artifactId='maven-eclipse-plugin']";

        Tools.insertDomNode(configuredPom, node, eclipsePluginXPath
                + "/pom:configuration/pom:additionalConfig/pom:file", NS_CONTEXT);
    }

}
