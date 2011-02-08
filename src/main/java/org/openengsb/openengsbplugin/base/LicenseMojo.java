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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.tools.MavenExecutor;
import org.openengsb.openengsbplugin.tools.Tools;

public abstract class LicenseMojo extends ConfiguredMojo {

    // #################################
    // set these in subclass constructor
    // #################################

    protected String wrappedGoal;
    protected String headerPath;

    // #################################

    private File licenseHeaderFile;

    @Override
    protected final void configureCoCMojo() throws MojoExecutionException {

        licenseHeaderFile = readHeaderStringAndwriteHeaderIntoTmpFile();
        FILES_TO_REMOVE_FINALLY.add(licenseHeaderFile);

        List<String> goals = new ArrayList<String>();
        goals.add(wrappedGoal);

        Properties userProperties = new Properties();
        userProperties.put("license.header", licenseHeaderFile.toURI().toString());
        userProperties.put("license.failIfMissing", "true");
        userProperties.put("license.aggregate", "true");
        userProperties.put("license.strictCheck", "true");

        MavenExecutor licenseMojoExecutor = getNewMavenExecutor(this);
        licenseMojoExecutor.addGoals(goals);
        licenseMojoExecutor.addUserProperties(userProperties);

        licenseMojoExecutor.setRecursive(true);
        licenseMojoExecutor.addActivatedProfiles(Arrays.asList(new String[] { cocProfile }));

        addMavenExecutor(licenseMojoExecutor);
    }

    @Override
    protected final void validateIfExecutionIsAllowed() throws MojoExecutionException {
    }

    private File readHeaderStringAndwriteHeaderIntoTmpFile() throws MojoExecutionException {
        try {

            String headerString = IOUtils.toString(getClass().getClassLoader().getResourceAsStream(headerPath));
            File generatedFile = Tools.generateTmpFile(headerString, ".txt");
            return generatedFile;
        } catch (Exception e) {
            throw new MojoExecutionException("Couldn't create license header temp file!", e);
        }
    }

}
