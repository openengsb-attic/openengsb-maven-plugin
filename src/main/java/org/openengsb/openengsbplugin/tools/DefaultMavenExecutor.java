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

package org.openengsb.openengsbplugin.tools;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.maven.Maven;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.openengsb.tooling.pluginsuite.openengsbplugin.base.AbstractOpenengsbMojo;

public class DefaultMavenExecutor implements MavenExecutor {

    private static final Logger LOG = Logger.getLogger(DefaultMavenExecutor.class);

    private MavenExecutionRequest embeddedRequest;
    private File baseDir = null;
   
    private MavenSession session;
    private Maven maven;

    public DefaultMavenExecutor(AbstractOpenengsbMojo mojo) {
        session = mojo.getSession();
        maven = mojo.getMaven();
        init();
    }

    private void init() {
        LOG.trace("#############################");
        LOG.trace(String.format("session: %s", session));
        LOG.trace("#############################");
        baseDir = session.getRequest().getPom().getParentFile();
        LOG.trace(String.format("basedir: %s", baseDir.toURI().toString()));
        embeddedRequest = generateRequestFromWrapperRequest(session);
        clearProperties(embeddedRequest);
    }

    private void clearProperties(MavenExecutionRequest request) {
        request.getGoals().clear();
        request.getUserProperties().clear();
        request.getActiveProfiles().clear();
        request.getInactiveProfiles().clear();
    }

    public void addGoals(List<String> goals) {
        if (goals != null) {
            embeddedRequest.getGoals().addAll(goals);
        }
    }

    public void addActivatedProfiles(List<String> activatedProfiles) {
        if (activatedProfiles != null) {
            embeddedRequest.getActiveProfiles().addAll(activatedProfiles);
        }
    }

    public void addDeactivatedProfiles(List<String> deactivatedProfiles) {
        if (deactivatedProfiles != null) {
            embeddedRequest.getInactiveProfiles().addAll(deactivatedProfiles);
        }
    }

    public void addUserProperties(Properties userproperties) {
        if (userproperties != null) {
            embeddedRequest.getUserProperties().putAll(userproperties);
        }
    }

    public void addProperties(List<String> goals, List<String> activatedProfiles, List<String> deactivatedProfiles,
            Properties userproperties) {
        addGoals(goals);
        addActivatedProfiles(activatedProfiles);
        addDeactivatedProfiles(deactivatedProfiles);
        addUserProperties(userproperties);
    }

    @Override
    public void setRecursive(boolean recursive) {
        embeddedRequest.setRecursive(recursive);
    }

    @Override
    public void setInterActiveMode(boolean interactiveMode) {
        embeddedRequest.setInteractiveMode(interactiveMode);
    }

    @Override
    public void setCustomPomFile(File pomFile) {
        LOG.trace(String.format("setting custom pom: %s", pomFile.toURI().toString()));
        embeddedRequest.setPom(pomFile);
        LOG.trace(String.format("setting basedir: %s", baseDir.toURI().toString()));
        embeddedRequest.setBaseDirectory(baseDir);
    }

    @Override
    public void execute(Log log) throws MojoExecutionException {
        printExecutionStartInfoLog(log);

        LOG.trace(String.format("basedir of embedded request: %s", embeddedRequest.getBaseDirectory()));
        LOG.trace(String.format("pomfile of embedded request: %s", embeddedRequest.getPom().toURI().toString()));

        LOG.trace("executing execution request with maven - start");
        MavenExecutionResult result = maven.execute(embeddedRequest);
        LOG.trace("executing execution request with maven - end");

        LOG.trace(String.format("basedir of embedded request: %s", embeddedRequest.getBaseDirectory()));

        printExecutionEndInfoLog(log);
        logAndPassOnExceptionIfAny(result, log);
    }

    private MavenExecutionRequest generateRequestFromWrapperRequest(MavenSession session) {
        MavenExecutionRequest wrapperRequest = session.getRequest();
        return DefaultMavenExecutionRequest.copy(wrapperRequest);
    }

    private void printExecutionStartInfoLog(Log log) {
        log.info("////////////////////////////////////////////////");
        log.info(String.format("EMBEDDED EXECUTION REQUESTS - BEGIN"));

        LOG.debug("####################");
        LOG.debug("# GOALS:");
        LOG.debug("####################");

        String goalsInfo = "";
        for (String goal : embeddedRequest.getGoals()) {
            goalsInfo += String.format("\t* %s\n", goal);
        }
        LOG.debug(goalsInfo);

        LOG.debug("####################");
        LOG.debug("# ACTIVE PROFILES:");
        LOG.debug("####################");

        if (embeddedRequest.getActiveProfiles() != null && embeddedRequest.getActiveProfiles().size() > 0) {
            String profilesInfo = "";
            for (String profile : embeddedRequest.getActiveProfiles()) {
                profilesInfo += String.format("\t* %s\n", profile);
            }
            LOG.debug(profilesInfo);
        }

        LOG.debug("####################");
        LOG.debug("# INACTIVE PROFILES:");
        LOG.debug("####################");

        if (embeddedRequest.getInactiveProfiles() != null && embeddedRequest.getInactiveProfiles().size() > 0) {
            String profilesInfo = "";
            for (String profile : embeddedRequest.getInactiveProfiles()) {
                profilesInfo += String.format("\t* %s\n", profile);
            }
            log.debug(profilesInfo);
        }

        LOG.debug("####################");
        LOG.debug("# PROPERTIES:");
        LOG.debug("####################");

        if (embeddedRequest.getUserProperties() != null && embeddedRequest.getUserProperties().size() > 0) {
            String propertiesInfo = "";
            for (Object propName : embeddedRequest.getUserProperties().keySet()) {
                propertiesInfo += String.format("\t* %s=%s\n", propName,
                        embeddedRequest.getUserProperties().get(propName));
            }
            LOG.debug(propertiesInfo);
        }
    }

    private void printExecutionEndInfoLog(Log log) {
        log.info(String.format("EMBEDDED EXECUTION REQUESTS - END"));
        log.info("////////////////////////////////////////////////");
    }

    private void logAndPassOnExceptionIfAny(MavenExecutionResult result, Log log) throws MojoExecutionException {
        if (result.hasExceptions()) {
            log.warn("###################");
            log.warn("The following exceptions occured during execution:");
            for (Throwable t : result.getExceptions()) {
                log.warn("--------");
                log.warn(t);
            }
            log.warn("###################");
            Throwable ex = result.getExceptions().get(0);
            Throwable cause = ex.getCause();
            String errmsg = cause != null ? cause.getMessage() : ex.getMessage();
            throw new MojoExecutionException(String.format("%s\nFAIL - see log statements above for additional info",
                    errmsg));
        }
    }

}
