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
import java.util.HashMap;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.openengsb.openengsbplugin.base.AbstractOpenengsbMojo;
import org.openengsb.openengsbplugin.tools.OpenEngSBJavaRunner;
import org.ops4j.pax.runner.platform.internal.CommandLineBuilder;

/**
 * Equivalent to executing run2.sh per hand after build by mvn clean install.
 *
 * @goal provision
 *
 * @inheritedByDefault false
 *
 * @requiresProject true
 * 
 * @aggregator true
 */
public class Provision extends AbstractOpenengsbMojo {
    
    private static String SCRIPT_PATH_DEFAULT = "etc/scripts/run2.sh";

    /**
     * The (relative) path to the script to be run for provisioning
     * 
     * @parameter expression="${scriptPath}" 
     */
    private String scriptPath;

    @Override
    protected void validateIfExecutionIsAllowed() throws MojoExecutionException {
    }

    @Override
    protected void configure() throws MojoExecutionException {
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(scriptPath == null) {
            scriptPath = SCRIPT_PATH_DEFAULT;
        }
        
    	File scriptFile = new File(scriptPath);

    	if(! scriptFile.isFile()) {
    		throw new MojoExecutionException(String.format("Provision script not found at '%s'", scriptFile.getAbsolutePath()));
        }

        throw new MojoExecutionException(String.format("Please use provision script provided at '%s'", scriptFile.getAbsolutePath()));
    }

}
