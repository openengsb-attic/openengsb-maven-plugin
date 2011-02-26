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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.openengsb.openengsbplugin.tools.Tools;
import org.openengsb.openengsbplugin.xml.OpenEngSBMavenPluginNSContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public abstract class ConfiguredMojo extends MavenExecutorMojo {
    
    public enum PomRestoreMode {
        CLEAN, RESTORE_BACKUP
    }

    private static final Logger LOG = Logger.getLogger(ConfiguredMojo.class);

    // #################################
    // set these in subclass constructor
    // #################################
    
    protected ArrayList<String> configs = new ArrayList<String>();

    // #################################

    protected String cocProfile;
    private String cocProfileToDeleteXpath;

    protected static final OpenEngSBMavenPluginNSContext NS_CONTEXT = new OpenEngSBMavenPluginNSContext();
    private static final String POM_PROFILE_XPATH = "/pom:project/pom:profiles";

    protected static final List<File> FILES_TO_REMOVE_FINALLY = new ArrayList<File>();
    
    private File backupOriginalPom;
    
    private PomRestoreMode pomRestoreMode = PomRestoreMode.RESTORE_BACKUP;
    private boolean pomCleanedSuccessfully = false;
    
    private Node licenseHeaderComment = null;

    /**
     * If set to "true" prints the temporary pom to the console.
     * 
     * @parameter expression="${debugMode}" default-value="false"
     */
    private boolean debugMode;

    @Override
    protected final void configure() throws MojoExecutionException {
        LOG.trace("-> configure");
        cocProfile = UUID.randomUUID().toString();
        configureTmpPom(cocProfile);
        cocProfileToDeleteXpath = String.format("/pom:project/pom:profiles/pom:profile[pom:id[text()='%s']]",
                cocProfile);
        configureCoCMojo();
    }
    
    /**
     * Configure how to restore (remove the unnecessary temporary profile) the
     * pom after successful mojo execution.
     * 
     * @param mode <ul>
     *        <li>{@link PomRestoreMode#RESTORE_BACKUP}: replace the pom with
     *        the backup which has been created at mojo startup - this is the
     *        default setting for all CoC mojos</li>
     *        <li>{@link PomRestoreMode#CLEAN}: don't replace the pom with the
     *        backup, only remove the profile which has been created for this
     *        mojo run - this setting can be useful if the wrapped mojo also
     *        modifies this pom (e.g. the maven-release-plugin changing version
     *        numbers), so that these changes don't get lost</li>
     *        <ul>
     */
    protected final void setPomRestoreMode(PomRestoreMode mode) {
        this.pomRestoreMode = mode;
    }

    @Override
    protected final void postExec() throws MojoExecutionException {
        if (pomRestoreMode == PomRestoreMode.CLEAN) {
            cleanPom(cocProfile);
            pomCleanedSuccessfully = true;
            afterPomCleaned();
        }
    }
    
    /**
     * Template method which may be overwritten by subclasses. It gets executed
     * iff {@link PomRestoreMode} is set to {@link PomRestoreMode#CLEAN} and the
     * pom has been cleaned successfully. For usage example see
     * {@link ReleaseMojo#afterPomCleaned()}
     */
    protected void afterPomCleaned() throws MojoExecutionException {
    }

    @Override
    protected final void postExecFinally() {
        if (pomRestoreMode == PomRestoreMode.RESTORE_BACKUP || pomRestoreMode == PomRestoreMode.CLEAN
                && !pomCleanedSuccessfully) {
            restoreOriginalPom();
        }
        cleanUp();
    }
    
    private void cleanPom(String profile) throws MojoExecutionException {
        LOG.trace("cleanPom()");
        try {
            Document docToClean = parseProjectPom();
            
            if (!Tools.removeNode(cocProfileToDeleteXpath, docToClean, NS_CONTEXT, true)) {
                throw new MojoExecutionException("Couldn't clean the pom!");
            }
            
            String cleanedContent = Tools.serializeXML(docToClean);
            
            writeIntoPom(cleanedContent);
        } catch (Exception e) {
            if (e instanceof MojoExecutionException) {
                throw (MojoExecutionException) e;
            } else {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
        LOG.trace("pom cleaned successfully");
    }
    
    private void restoreOriginalPom() {
        LOG.trace("-> restoreOriginalPom");
        try {
            FileUtils.copyFile(backupOriginalPom, getSession().getRequest().getPom());
        } catch (Exception e) {
            // do nothing
        }
    }

    protected abstract void configureCoCMojo() throws MojoExecutionException;

    private void configureTmpPom(String profileName) throws MojoExecutionException {
        try {
            backupOriginalPom = backupOriginalPom(getSession().getRequest().getPom());
            FILES_TO_REMOVE_FINALLY.add(backupOriginalPom);
            
            Document pomDocumentToConfigure = parseProjectPom();
            Document configDocument = collectConfigsAndBuildProfile();
            
            modifyMojoConfiguration(configDocument);

            insertConfigProfileIntoOrigPom(pomDocumentToConfigure, configDocument,
                    profileName);

            String serializedXml = Tools.serializeXML(pomDocumentToConfigure);

            if (debugMode) {
                System.out.print(serializedXml);
            }
            
            writeIntoPom(serializedXml);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            throw new MojoExecutionException("Couldn't configure temporary pom for this execution!", e);
        }
    }
    
    /**
     * If you want to modify the xml configuration of the mojo (e.g. add some
     * configuration which you only know at runtime), then this is the place
     * where to do it. Simply overwrite this method in your subclass.
     */
    protected void modifyMojoConfiguration(Document mojoConfiguration) throws MojoExecutionException {
    }
    
    private String addHeader(String pomContent) throws IOException {
        String result = "";
        StringReader stringReader = new StringReader(pomContent);
        BufferedReader br = new BufferedReader(stringReader);

        String line;
        do {
            line = br.readLine();
            result += line + "\n";
        } while (!line.contains("<?xml"));

        result += "<!--";
        result += licenseHeaderComment.getTextContent();
        result += "-->\n\n";

        line = br.readLine();
        while (line != null) {
            result += line + "\n";
            line = br.readLine();
        }

        br.close();
        
        licenseHeaderComment = null;
        
        return result;
    }

    private Document parseProjectPom() throws Exception {
        Document doc = Tools.parseXMLFromString(FileUtils.readFileToString(getSession().getRequest().getPom()));
        tryExtractLicenseHeader(doc);
        return doc;
    }
    
    private void tryExtractLicenseHeader(Document doc) {
        Node firstNode = doc.getChildNodes().item(0);
        if (firstNode.getNodeType() == Node.COMMENT_NODE) {
            LOG.trace(String.format("found license header with content:\n%s", firstNode.getNodeValue()));
            licenseHeaderComment = doc.removeChild(firstNode);
        }
    }

    private Document collectConfigsAndBuildProfile() throws ParserConfigurationException, SAXException, IOException,
            XPathExpressionException {

        ArrayList<Node> pluginNodes = new ArrayList<Node>();

        for (String configFilePath : configs) {
            Document configDocument = Tools.parseXMLFromString(IOUtils.toString(getClass().getClassLoader()
                    .getResourceAsStream(configFilePath)));
            Node configNode = Tools.evaluateXPath("/c:config", configDocument, NS_CONTEXT, XPathConstants.NODE,
                    Node.class);
            NodeList children = configNode.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                pluginNodes.add(children.item(i));
            }
        }
        
        Document profileDoc = Tools.newDOM();
        Element profileElement = profileDoc.createElement("profile");
        profileDoc.appendChild(profileElement);
        Element buildElement = profileDoc.createElement("build");
        profileElement.appendChild(buildElement);
        Element pluginsElement = profileDoc.createElement("plugins");
        buildElement.appendChild(pluginsElement);
        
        for (Node pluginNode : pluginNodes) {
            pluginsElement.appendChild(profileDoc.importNode(pluginNode, true));
        }

        return profileDoc;
    }

    private void insertConfigProfileIntoOrigPom(Document originalPom, Document mojoConfiguration,
            String profileName) throws XPathExpressionException {
        Node profileNode = mojoConfiguration.getFirstChild();
        
        Node idNode = mojoConfiguration.createElement("id");
        idNode.setTextContent(profileName);
        profileNode.insertBefore(idNode, profileNode.getFirstChild());

        Node importedProfileNode = originalPom.importNode(profileNode, true);

        Tools.insertDomNode(originalPom, importedProfileNode, POM_PROFILE_XPATH, NS_CONTEXT);
    }

    private void writeIntoPom(String content) throws IOException, URISyntaxException {
        if (licenseHeaderComment != null) {
            content = addHeader(content);
        }
        FileUtils.writeStringToFile(getSession().getRequest().getPom(), content + "\n");
    }

    private void cleanUp() {
        LOG.trace("-> cleanup()");
        for (File f : FILES_TO_REMOVE_FINALLY) {
            FileUtils.deleteQuietly(f);
        }
    }
    
    private File backupOriginalPom(File originalPom) throws IOException {
        return Tools.generateTmpFile(FileUtils.readFileToString(originalPom), ".xml");
    }

}
