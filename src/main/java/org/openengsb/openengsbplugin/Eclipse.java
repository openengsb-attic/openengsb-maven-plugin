package org.openengsb.openengsbplugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.base.ConfiguredMojo;
import org.openengsb.openengsbplugin.tools.MavenExecutor;

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

    public Eclipse() {
        configs.add("eclipse/eclipseConfig.xml");
    }

    @Override
    protected void configureCoCMojo() throws MojoExecutionException {
        List<String> goals = new ArrayList<String>();
        goals.add("eclipse:eclipse");
        
        MavenExecutor eclipseMojoExecutor = getNewMavenExecutor(this);
        eclipseMojoExecutor.addGoals(goals);
        eclipseMojoExecutor.setRecursive(true);
        eclipseMojoExecutor.addActivatedProfiles(Arrays.asList(new String[] { cocProfile }));
        
        addMavenExecutor(eclipseMojoExecutor);
    }

    @Override
    protected void validateIfExecutionIsAllowed() throws MojoExecutionException {
    }

}
