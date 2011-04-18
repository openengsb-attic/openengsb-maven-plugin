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

import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.base.ConfiguredMojo;
import org.openengsb.openengsbplugin.base.LicenseMojo;
import org.openengsb.openengsbplugin.tools.MavenExecutor;
import org.w3c.dom.Document;

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

    /**
     * Defines path to a file where each line represents a pattern which to exclude from license check or license format
     * (additionally to the default excludes).
     *
     * @parameter expression="${additionalExcludes}"
     */
    private String additionalExcludes;

    public PrePush() {
        pomConfigs.put(
                "pom.xml",
                Arrays.asList(new String[]{ "license/licenseConfig.xml", "checkstyle/checkstyleConfig.xml",
                    "integrationTest/integrationTestConfig.xml" }));
    }

    @Override
    protected void configureCoCMojo() throws MojoExecutionException {
        List<String> goals = new ArrayList<String>();
        goals.add("clean");
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
    protected void modifyMojoConfiguration(String pomPath, Document configuredPom) throws MojoExecutionException {
        if (pomPath.equals("pom.xml")) {
            try {
                File licenseHeaderFile = LicenseMojo.readHeaderStringAndwriteHeaderIntoTmpFile();
                FILES_TO_REMOVE_FINALLY.add(licenseHeaderFile);
                LicenseMojo.insertGoalAndSetHeaderPath(configuredPom, cocProfileXpath, "check",
                        licenseHeaderFile.getAbsolutePath());
                LicenseMojo.addExcludes(configuredPom, cocProfileXpath, additionalExcludes);

                File checkstyleCheckerConfigTmp = Checkstyle.createCheckstyleCheckerConfiguration();
                FILES_TO_REMOVE_FINALLY.add(checkstyleCheckerConfigTmp);
                Checkstyle.insertCheckstyleConfigLocation(configuredPom, cocProfileXpath, checkstyleCheckerConfigTmp);
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

    @Override
    protected void validateIfExecutionIsAllowed() throws MojoExecutionException {
    }

}
