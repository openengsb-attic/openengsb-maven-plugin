package org.openengsb.openengsbplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.base.ConfiguredMojo;
import org.openengsb.openengsbplugin.tools.MavenExecutor;

/**
 * Builds documentation, site etc. Should only be invoked from the root dir.
 *  
 * @goal docs
 * 
 * @inheritedByDefault false
 * 
 * @requiresProject true
 * 
 * @aggregator true
 * 
 */
public class Docs extends ConfiguredMojo {
    
    public Docs() {
        pomConfigs.put("docs/pom.xml", Arrays.asList(new String[] { "docs/docsConfig.xml" }));
        pomConfigs.put("docs/examples/pom.xml", Arrays.asList(new String[] { "docs/examplesConfig.xml" }));
        pomConfigs.put("docs/homepage/pom.xml", Arrays.asList(new String[] { "docs/homepageConfig.xml" }));
        pomConfigs.put("docs/manual/pom.xml", Arrays.asList(new String[] { "docs/manualConfig.xml" }));
    }

    @Override
    protected void configureCoCMojo() throws MojoExecutionException {
        List<String> goals = new ArrayList<String>();
        goals.add("clean");
        goals.add("install");
        
        List<String> activeProfiles = new ArrayList<String>();
        activeProfiles.add(cocProfile);
        
        MavenExecutor docsMojoExecutor = getNewMavenExecutor(this);
        docsMojoExecutor.addGoals(goals);
        docsMojoExecutor.setRecursive(true);
        docsMojoExecutor.addActivatedProfiles(activeProfiles);
        
        addMavenExecutor(docsMojoExecutor);
    }

    @Override
    protected void validateIfExecutionIsAllowed() throws MojoExecutionException {
    }

}
