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

package org.openengsb.openengsbplugin.base;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.tools.MavenExecutor;

public abstract class ReleaseMojo extends ConfiguredMojo {
    
    private static final String CLEANED_POM_COMMIT_MSG = "[openengsb-maven-plugin]: cleaning pom";

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
        configs.add("release/releaseCommonConfig.xml");
    }

    protected abstract String getReleaseProfile();

    /*
     * configures maven executors for release:prepare and release:perform
     */
    @Override
    protected final void configureCoCMojo() throws MojoExecutionException {
        List<String> activatedProfilesForPrepareAndPerform = new ArrayList<String>();
        activatedProfilesForPrepareAndPerform.add(cocProfile);
        activatedProfilesForPrepareAndPerform.add(getReleaseProfile());

        List<String> releasePrepareGoals = new ArrayList<String>();
        releasePrepareGoals.add("release:prepare");

        Properties releasePrepareUserProps = new Properties();
        releasePrepareUserProps.put("maven.test.skip", "true");

        MavenExecutor releasePrepareExecutor = getNewMavenExecutor(this);
        releasePrepareExecutor.addGoals(releasePrepareGoals);
        releasePrepareExecutor.addUserProperties(releasePrepareUserProps);
        releasePrepareExecutor.addActivatedProfiles(activatedProfilesForPrepareAndPerform);

        releasePrepareExecutor.setRecursive(true);

        addMavenExecutor(releasePrepareExecutor);

        List<String> releasePerformGoals = new ArrayList<String>();
        releasePerformGoals.add("release:perform");

        Properties releasePerformUserProps = new Properties();
        releasePerformUserProps.put("maven.test.skip", "true");
        releasePerformUserProps.put("connectionUrl", connectionUrl);

        MavenExecutor releasePerformExecutor = getNewMavenExecutor(this);
        releasePerformExecutor.addGoals(releasePerformGoals);
        releasePerformExecutor.addUserProperties(releasePerformUserProps);
        releasePerformExecutor.addActivatedProfiles(activatedProfilesForPrepareAndPerform);

        releasePerformExecutor.setRecursive(true);

        addMavenExecutor(releasePerformExecutor);
        
        setPomRestoreMode(PomRestoreMode.CLEAN);
    }

    @Override
    protected final void validateIfExecutionIsAllowed() throws MojoExecutionException {
        throwErrorIfProjectIsNotExecutedInRootDirectory();
    }

    @Override
    protected final void afterPomCleaned() throws MojoExecutionException {
        commitCleanedPom();
    }

    /*
     * commit cleaned pom after the maven executors for release:prepare and
     * release:perform have finshed work
     */
    private void commitCleanedPom() throws MojoExecutionException {
        List<String> finalCommitGoals = new ArrayList<String>();
        finalCommitGoals.add("scm:checkin");

        Properties finalCommitUserProps = new Properties();
        finalCommitUserProps.put("message", CLEANED_POM_COMMIT_MSG);
        finalCommitUserProps.put("pushChanges", "false");

        MavenExecutor finalCommitExecutor = getNewMavenExecutor(this);
        finalCommitExecutor.addGoals(finalCommitGoals);
        finalCommitExecutor.addUserProperties(finalCommitUserProps);

        /*
         * execute directly (instead of addMavenExecutor - the maven executors
         * queue has been already worked off)
         */

        finalCommitExecutor.execute(getLog());

    }
    

}
