package org.openengsb.openengsbplugin;

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.base.ConfiguredMojo;
import org.openengsb.openengsbplugin.tools.MavenExecutor;

/**
 *  
 * @goal prePush
 * 
 * @inheritedByDefault false
 * 
 * @requiresProject true
 * 
 * @aggregator true
 * 
 */
public class PrePush extends ConfiguredMojo {
    
    public PrePush() {
        configs.add("integrationTest/integrationTestConfig.xml");
    }

    @Override
    protected void configureCoCMojo() throws MojoExecutionException {
        List<String> goals = new ArrayList<String>();
        goals.add("clean");
        goals.add("openengsb:licenseCheck");
        goals.add("openengsb:checkstyle");
        goals.add("install");
        
        List<String> activeProfiles = new ArrayList<String>();
        activeProfiles.add(cocProfile);
        
        MavenExecutor prePushMojoExecutor = getNewMavenExecutor(this);
        prePushMojoExecutor.addGoals(goals);
        prePushMojoExecutor.setRecursive(true);
        prePushMojoExecutor.addActivatedProfiles(activeProfiles);
        
        addMavenExecutor(prePushMojoExecutor);
    }

    @Override
    protected void validateIfExecutionIsAllowed() throws MojoExecutionException {
    }

}
