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
import org.openengsb.openengsbplugin.tools.MavenExecutor;

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
        List<String> activatedProfiles1and2 = new ArrayList<String>();
        activatedProfiles1and2.add(cocProfile);
        activatedProfiles1and2.add(getReleaseProfile());

        List<String> phase1Goals = new ArrayList<String>();
        phase1Goals.add("release:prepare");

        Properties phase1UserProps = new Properties();
        phase1UserProps.put("maven.test.skip", "true");

        MavenExecutor phase1Executor = getNewMavenExecutor(this);
        phase1Executor.addGoals(phase1Goals);
        phase1Executor.addUserProperties(phase1UserProps);
        phase1Executor.addActivatedProfiles(activatedProfiles1and2);

        phase1Executor.setRecursive(true);

        addMavenExecutor(phase1Executor);

        List<String> phase2Goals = new ArrayList<String>();
        phase2Goals.add("release:perform");

        Properties phase2UserProps = new Properties();
        phase2UserProps.put("maven.test.skip", "true");
        phase2UserProps.put("connectionUrl", connectionUrl);

        MavenExecutor phase2Executor = getNewMavenExecutor(this);
        phase2Executor.addGoals(phase2Goals);
        phase2Executor.addUserProperties(phase2UserProps);
        phase2Executor.addActivatedProfiles(activatedProfiles1and2);

        phase2Executor.setRecursive(true);

        addMavenExecutor(phase2Executor);
        
        setPomRestoreMode(PomRestoreMode.CLEAN);
    }

    @Override
    protected final void validateIfExecutionIsAllowed() throws MojoExecutionException {
        throwErrorIfProjectIsNotExecutedInRootDirectory();
    }

    @Override
    protected void afterPomCleaned() throws MojoExecutionException {
        commitCleanedPom();
    }
    
    private void commitCleanedPom() throws MojoExecutionException {
        List<String> phase3Goals = new ArrayList<String>();
        phase3Goals.add("scm:checkin");

        Properties phase3UserProps = new Properties();
        phase3UserProps.put("message", "[openengsb-maven-plugin]: cleaning pom");
        phase3UserProps.put("pushChanges", "false");

        MavenExecutor phase3Exectuor = getNewMavenExecutor(this);
        phase3Exectuor.addGoals(phase3Goals);
        phase3Exectuor.addUserProperties(phase3UserProps);

        /*
         * execute directly (instead of addMavenExecutor - the maven executors
         * queue has been already worked off)
         */

        phase3Exectuor.execute(getLog());

    }
    

}
