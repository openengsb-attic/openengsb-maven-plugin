package org.openengsb.openengsbplugin;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.base.ConfiguredMojo;
import org.openengsb.openengsbplugin.tools.MavenExecutor;
import org.openengsb.openengsbplugin.tools.Tools;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

// TODO rename to prePush

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
    
    private static final Logger LOG = Logger.getLogger(Checkstyle.class);
    
    private String checkstylePath = "checkstyleMojo/checkstyle.xml";

    private File checkstyleCheckerConfig;
    
    private static final String CHECKSTYLE_CHECKER_PROFILE_XPATH = "/cs:checkstyleMojo/cs:profile";
    private static final String CHECKSTYLE_CHECKER_CONFIG_XPATH = CHECKSTYLE_CHECKER_PROFILE_XPATH
            + "/cs:build/cs:plugins/cs:plugin/cs:configuration";

    public Checkstyle() {
        configPath = "checkstyleMojo/checkstyleConfig.xml";
        configProfileXpath = CHECKSTYLE_CHECKER_PROFILE_XPATH;
    }

    @Override
    protected void configureCoCMojo() throws MojoExecutionException {        
        List<String> goals = new ArrayList<String>();
        goals.add("clean");
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
    protected final void modifyMojoConfiguration(Document mojoConfiguration) throws MojoExecutionException {
        try {
            checkstyleCheckerConfig = createCheckstyleCheckerConfiguration();
            FILES_TO_REMOVE_FINALLY.add(checkstyleCheckerConfig);
            insertCheckstyleConfigLocation(mojoConfiguration);
        } catch (XPathExpressionException e) {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
    
    private void insertCheckstyleConfigLocation(Document mojoConfiguration)
        throws XPathExpressionException {
        Node node = mojoConfiguration.createElement("configLocation");
        node.setTextContent(checkstyleCheckerConfig.toURI().toString());

        Node configurationNode = Tools.evaluateXPath(CHECKSTYLE_CHECKER_CONFIG_XPATH, mojoConfiguration, NS_CONTEXT,
                XPathConstants.NODE, Node.class);

        configurationNode.appendChild(node);
    }
    
    

}
