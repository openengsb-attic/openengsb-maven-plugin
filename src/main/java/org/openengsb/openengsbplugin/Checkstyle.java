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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.base.ConfiguredMojo;
import org.openengsb.openengsbplugin.tools.MavenExecutor;
import org.openengsb.openengsbplugin.tools.Tools;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *  
 * @goal checkstyle
 * 
 * @inheritedByDefault false
 * 
 * @requiresProject true
 * 
 * @aggregator true
 * 
 */
public class Checkstyle extends ConfiguredMojo {
    
    private String checkstylePath = "checkstyle/checkstyle.xml";

    private File checkstyleCheckerConfig;
    
    /**
     * If set to "true" the clean phase is skipped.
     * 
     * @parameter expression="${skipClean}" default-value="false"
     */
    private boolean skipClean;

    public Checkstyle() {
        configs.add("checkstyle/checkstyleConfig.xml");
    }

    @Override
    protected void configureCoCMojo() throws MojoExecutionException {
        List<String> goals = new ArrayList<String>();
        if (!skipClean) {
            goals.add("clean");
        }
        goals.add("install");

        Properties userProperties = new Properties();
        userProperties.put("maven.test.skip", "true");

        MavenExecutor checkstyleMojoExecutor = getNewMavenExecutor(this);
        checkstyleMojoExecutor.addGoals(goals);
        checkstyleMojoExecutor.addUserProperties(userProperties);

        checkstyleMojoExecutor.setRecursive(true);
        checkstyleMojoExecutor.addActivatedProfiles(Arrays.asList(new String[] { cocProfile }));

        addMavenExecutor(checkstyleMojoExecutor);
    }

    @Override
    protected void validateIfExecutionIsAllowed() throws MojoExecutionException {
    }
    
    private File createCheckstyleCheckerConfiguration() throws MojoExecutionException {
        try {
            String headerString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(checkstylePath));
            File generatedFile = Tools.generateTmpFile(headerString, ".xml");
            return generatedFile;
        } catch (Exception e) {
            throw new MojoExecutionException("Couldn't create checkstyle temp file!", e);
        }
    }

    @Override
    protected final void modifyMojoConfiguration(Document configuredPom) throws MojoExecutionException {
        try {
            checkstyleCheckerConfig = createCheckstyleCheckerConfiguration();
            FILES_TO_REMOVE_FINALLY.add(checkstyleCheckerConfig);
            insertCheckstyleConfigLocation(configuredPom);
        } catch (XPathExpressionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
    
    private void insertCheckstyleConfigLocation(Document configuredPom) throws XPathExpressionException {
        Node node = configuredPom.createElementNS(POM_NS_URI, "configLocation");
        node.setTextContent(checkstyleCheckerConfig.toURI().toString());
        
        Tools.insertDomNode(configuredPom, node, cocProfileXpath + "/pom:build/pom:plugins/pom:plugin"
                + "[pom:groupId='org.apache.maven.plugins' and pom:artifactId='maven-checkstyle-plugin']"
                + "/pom:configuration", NS_CONTEXT);
    }
    
    

}
