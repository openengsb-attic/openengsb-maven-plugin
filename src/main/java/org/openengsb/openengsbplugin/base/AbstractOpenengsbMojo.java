/**
 * Licensed to the Austrian Association for
 * Software Tool Integration (AASTI) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openengsb.openengsbplugin.base;

import org.apache.maven.Maven;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;

public abstract class AbstractOpenengsbMojo extends AbstractMojo {

    /**
     * @parameter expression="${project}"
     */
    private MavenProject project;

    /**
     * @parameter expression="${session}"
     */
    private MavenSession session;

    /**
     * @component role="org.apache.maven.Maven"
     */
    private Maven maven;

    protected abstract void configure() throws MojoExecutionException;

    protected abstract void validateIfExecutionIsAllowed() throws MojoExecutionException;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

    }

    public final MavenProject getProject() {
        return project;
    }

    public final MavenSession getSession() {
        return session;
    }

    public final Maven getMaven() {
        return maven;
    }

    protected final void throwErrorIfWrapperRequestIsRecursive() throws MojoExecutionException {
        if (!getProject().isExecutionRoot()) {
            String msg = "Please execute this mojo with the maven -N flag!\n";
            msg += "Hint: This doesn't mean that the embedded request isn't executed recursivley ";
            msg += "(This depends on the mojo implementation)";
            throw new MojoExecutionException(msg);
        }
    }

    protected final void throwErrorIfProjectIsNotExecutedInRootDirectory() throws MojoExecutionException {
        if (getProject().hasParent() && !getProject().getParent().getArtifactId().equals("openengsb-root")) {
            throw new MojoExecutionException("Please invoke this mojo only in the OpenEngSB root!");
        }
    }

}
