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

package org.openengsb.openengsbplugin.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.tooling.pluginsuite.openengsbplugin.tools.MavenExecutor;

public abstract class ReleaseMojo extends ConfiguredMojo {

    /**
     * The SCM URL to checkout from. If omitted, the one from the
     * release.properties file is used, followed by the URL from the current
     * POM.
     * 
     * @parameter expression="${connectionUrl}"
     * 
     * @required
     */
    protected String connectionUrl;

    public ReleaseMojo() {
        configPath = "releaseMojo/releaseCommonConfig.xml";
        configProfileXpath = "/rcc:releaseCommonConfig/rcc:profile";
    }

    protected abstract String getReleaseProfile();

    @Override
    protected void configureCoCMojo() throws MojoExecutionException {
        List<String> activatedProfiles = new ArrayList<String>();
        activatedProfiles.add(cocProfile);
        activatedProfiles.add(getReleaseProfile());

        List<String> phaseOneGoals = new ArrayList<String>();
        phaseOneGoals.add("release:prepare");

        Properties phaseOneUserProps = new Properties();
        phaseOneUserProps.put("maven.test.skip", "true");

        MavenExecutor phaseOneExecutor = getNewMavenExecutor(this);
        phaseOneExecutor.addGoals(phaseOneGoals);
        phaseOneExecutor.addUserProperties(phaseOneUserProps);
        phaseOneExecutor.addActivatedProfiles(activatedProfiles);

        phaseOneExecutor.setRecursive(true);
        phaseOneExecutor.setCustomPomFile(cocPom);

        addMavenExecutor(phaseOneExecutor);

        List<String> phaseTwoGoals = new ArrayList<String>();
        phaseTwoGoals.add("release:perform");

        Properties phaseTwoUserProps = new Properties();
        phaseTwoUserProps.put("maven.test.skip", "true");
        phaseTwoUserProps.put("connectionUrl", connectionUrl);

        MavenExecutor phaseTwoExecutor = getNewMavenExecutor(this);
        phaseTwoExecutor.addGoals(phaseTwoGoals);
        phaseTwoExecutor.addUserProperties(phaseTwoUserProps);
        phaseTwoExecutor.addActivatedProfiles(activatedProfiles);

        phaseTwoExecutor.setRecursive(true);
        phaseTwoExecutor.setCustomPomFile(cocPom);

        addMavenExecutor(phaseTwoExecutor);
    }

    @Override
    protected final void validateIfExecutionIsAllowed() throws MojoExecutionException {
        throwErrorIfProjectIsNotExecutedInRootDirectory();
    }

}
