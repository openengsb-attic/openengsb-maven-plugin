package org.openengsb.openengsbplugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.base.ConfiguredMojo;
import org.openengsb.openengsbplugin.tools.MavenExecutor;
import org.openengsb.openengsbplugin.tools.Tools;

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
    
    private String checkstylePath = "checkstyleMojo/checkstyle.xml";

    private File checkstyleCheckerConfig;

    public Checkstyle() {
        configPath = "checkstyleMojo/checkstyleConfig.xml";
        configProfileXpath = "/cs:checkstyleMojo/cs:profile";
    }

    @Override
    protected void configureCoCMojo() throws MojoExecutionException {
        checkstyleCheckerConfig = createCheckstyleCheckerConfiguration();
        FILES_TO_REMOVE_FINALLY.add(checkstyleCheckerConfig);
        
        List<String> goals = new ArrayList<String>();
        goals.add("checkstyle:check");
        
        Properties userProperties = new Properties();
        userProperties.put("configLocation", checkstyleCheckerConfig.getAbsolutePath());
        
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

}
