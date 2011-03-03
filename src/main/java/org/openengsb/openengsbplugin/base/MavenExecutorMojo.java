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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.openengsb.openengsbplugin.tools.DefaultMavenExecutor;
import org.openengsb.openengsbplugin.tools.MavenExecutor;

public abstract class MavenExecutorMojo extends AbstractOpenengsbMojo {

    public static final String OPENENGSB_ROOT_GROUP_ID = "org.openengsb";
    public static final String OPENENGSB_ROOT_ARTIFACT_ID = "openengsb-parent";

    private List<MavenExecutor> mavenExecutors = new ArrayList<MavenExecutor>();

    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        try {
            validateIfExecutionIsAllowed();
            configure();
            for (MavenExecutor executor : mavenExecutors) {
                executor.execute(getLog());
            }
            postExec();
        } finally {
            postExecFinally();
        }
    }

    protected void postExec() throws MojoExecutionException {
    }

    protected void postExecFinally() {
    }

    protected final MavenExecutor getNewMavenExecutor(AbstractOpenengsbMojo mojo) {
        return new DefaultMavenExecutor(mojo);
    }

    protected final void addMavenExecutor(MavenExecutor mavenExecutor) {
        mavenExecutors.add(mavenExecutor);
    }

}
