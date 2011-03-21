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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
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
    
    /**
     * If you wan't to merge some configuration into some other pom's except the
     * root pom (pom in the dir where invoke the plugin), please put entries of
     * the form
     * {@code (<relative path to pom>, <list of configurations from the resources>)}
     * into {@link ConfiguredMojo#pomConfigs}. The content of the
     * configuration files listed will get merged into the pom at the specified path.
     */
    protected HashMap<String, List<String>> pomConfigs = new HashMap<String, List<String>>();
    
    /**
     * Stores references to poms corresponding to keys in {@link ConfiguredMojo#pomConfigs}
     */
    private HashMap<String, File> poms = new HashMap<String, File>();
    
    /**
     * points to backups of modified poms in the local tmp dir. The key is the original
     * pom in the project (e.g. "docs/pom.xml"), where the value is the
     * reference to the file in the tmp dir.
     */
    private HashMap<File, File> pomBackups = new HashMap<File, File>();

    // #################################
    // set these in subclass constructor
    // #################################

    /**
     * Defines which configs (from {@code resources/xxx}) to merge into the pom of the current dir (where
     * the mojo is invoked). For examples please see constructor of every direct subclass of {@link ConfiguredMojo} (
     * e.g. {@link LicenseMojo#LicenseMojo()}.
     */
    protected ArrayList<String> configs = new ArrayList<String>();

    // #################################

    protected String cocProfile;
    protected String cocProfileXpath;

    protected static final OpenEngSBMavenPluginNSContext NS_CONTEXT = new OpenEngSBMavenPluginNSContext();
    protected static final String POM_NS_URI = NS_CONTEXT.getNamespaceURI("pom");
    private static final String POM_PROFILE_XPATH = "/pom:project/pom:profiles";
    
    private static final String CONFIGNODE_XPATH = "/c:config";
    private static final String PLUGINS_XPATH = CONFIGNODE_XPATH + "/pom:plugins/pom:plugin";
    private static final String MODULES_XPATH = CONFIGNODE_XPATH + "/pom:modules/pom:module";
    private static final String RESOURCES_XPATH = CONFIGNODE_XPATH + "/pom:resources/pom:resource";

    protected static final List<File> FILES_TO_REMOVE_FINALLY = new ArrayList<File>();
    
    private PomRestoreMode pomRestoreMode = PomRestoreMode.RESTORE_BACKUP;
    private boolean pomCleanedSuccessfully = false;
    
    protected Node licenseHeaderComment = null;

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
        cocProfileXpath = String.format("/pom:project/pom:profiles/pom:profile[pom:id[text()='%s']]",
                cocProfile);
        configureCoCMojo();
        
        pomConfigs.put("pom.xml", configs);
        poms.put("pom.xml", getSession().getRequest().getPom());
        
        checkForPoms();
        for (String pomPath : poms.keySet()) {
            configureTmpPom(pomPath, poms.get(pomPath), cocProfile);
        }
    }
    
    private void checkForPoms() throws MojoExecutionException {
        for (String pomPath : pomConfigs.keySet()) {
            File pom = new File(pomPath);
            if (!pom.exists()) {
                throw new MojoExecutionException(String.format("pom doesn't exist: %s", pomPath));
            }
            poms.put(pomPath, pom);
        }
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
            cleanPoms(cocProfile);
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
    
    /**
     * remove profile with id {@code profile} from the pom (used to clean pom
     * from configuration which gets merged into it during runtime)
     */
    private void cleanPoms(String profile) throws MojoExecutionException {
        LOG.trace("cleanPom()");
        try {
            for (File pomFile : poms.values()) {
                Document docToClean = parsePom(pomFile);
                
                if (!Tools.removeNode(cocProfileXpath, docToClean, NS_CONTEXT, true)) {
                    throw new MojoExecutionException("Couldn't clean the pom!");
                }
                
                String cleanedContent = Tools.serializeXML(docToClean);
                
                writeIntoPom(getSession().getRequest().getPom(), cleanedContent);
            }
        } catch (Exception e) {
            if (e instanceof MojoExecutionException) {
                throw (MojoExecutionException) e;
            } else {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
        LOG.trace("pom cleaned successfully");
    }
    
    /**
     * restore backup of the pom which has been created on mojo start
     */
    private void restoreOriginalPom() {
        LOG.trace("-> restoreOriginalPom");
        try {
            for (File pomFile : poms.values()) {
                FileUtils.copyFile(pomBackups.get(pomFile), pomFile);
            }
        } catch (Exception e) {
            // do nothing
        }
    }

    /**
     * template method for subclasses where you can declare which goals should
     * be used and which configuration should be merged into the poms
     */
    protected abstract void configureCoCMojo() throws MojoExecutionException;

    private void configureTmpPom(String pomPath, File pomFile, String profileName) throws MojoExecutionException {
        try {
            
            File backupPom = backupOriginalPom(pomFile);
            pomBackups.put(pomFile, backupPom);
            
            FILES_TO_REMOVE_FINALLY.add(backupPom);
            
            Document pomDocumentToConfigure = parsePom(pomFile);
            Document configDocument = collectConfigsAndBuildProfile(pomPath);
            
            insertConfigProfileIntoOrigPom(pomDocumentToConfigure, configDocument,
                    profileName);
            
            modifyMojoConfiguration(pomPath, pomDocumentToConfigure);

            String serializedXml = Tools.serializeXML(pomDocumentToConfigure);

            if (debugMode) {
                System.out.print(serializedXml);
            }
            
            writeIntoPom(pomFile, serializedXml);
        } catch (Exception e) {
            LOG.warn(e.getMessage(), e);
            throw new MojoExecutionException("Couldn't configure temporary pom for this execution!", e);
        }
    }
    
    /**
     * If you want to modify the xml configuration of the mojo (e.g. add some
     * configuration which you is not known in
     * {@link ConfiguredMojo#configureTmpPom(String)}), then this is the place
     * where to do it. Simply overwrite this method in your subclass.
     */
    protected void modifyMojoConfiguration(String pomPath, Document configuredPom) throws MojoExecutionException {
    }
    
    protected String addHeader(String content) throws IOException {
        String result = "";
        StringReader stringReader = new StringReader(content);
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
    
    /**
     * parse pom and remove license header if available
     */
    private Document parsePom(File pom) throws Exception {
        Document doc = Tools.parseXMLFromString(FileUtils.readFileToString(pom));
        tryExtractLicenseHeader(doc);
        return doc;
    }
    
    protected void tryExtractLicenseHeader(Document doc) {
        Node firstNode = doc.getChildNodes().item(0);
        if (firstNode.getNodeType() == Node.COMMENT_NODE) {
            LOG.trace(String.format("found license header with content:\n%s", firstNode.getNodeValue()));
            licenseHeaderComment = doc.removeChild(firstNode);
        }
    }

    /**
     * Build a profile of alle specified config files which later gets merged into
     * the pom.
     */
    private Document collectConfigsAndBuildProfile(String pomPath) throws ParserConfigurationException, SAXException,
        IOException, XPathExpressionException {

        Document profileDoc = Tools.newDOM();
        Element profileElement = profileDoc.createElementNS(POM_NS_URI, "profile");
        profileDoc.appendChild(profileElement);
        Element buildElement = profileDoc.createElementNS(POM_NS_URI, "build");
        profileElement.appendChild(buildElement);
        Element pluginsElement = profileDoc.createElementNS(POM_NS_URI, "plugins");
        buildElement.appendChild(pluginsElement);
        Element modulesElement = profileDoc.createElementNS(POM_NS_URI, "modules");
        buildElement.appendChild(modulesElement);
        Element resourcesElement = profileDoc.createElementNS(POM_NS_URI, "resources");
        buildElement.appendChild(resourcesElement);

        /*
         * if you want to support further profile configuration (current:
         * <plugin>, <modules>, <resources>) this is the place where to put it
         */

        for (String configFilePath : pomConfigs.get(pomPath)) {
            Document configDocument = Tools.parseXMLFromString(IOUtils.toString(getClass().getClassLoader()
                    .getResourceAsStream(configFilePath)));
            collectFromXPathAndImportConfigurations(PLUGINS_XPATH, configDocument, profileDoc, buildElement,
                    pluginsElement);
            collectFromXPathAndImportConfigurations(MODULES_XPATH, configDocument, profileDoc, buildElement,
                    modulesElement);
            collectFromXPathAndImportConfigurations(RESOURCES_XPATH, configDocument, profileDoc, buildElement,
                    resourcesElement);
        }

        return profileDoc;
    }
    
    private void collectFromXPathAndImportConfigurations(String xpath, Document source, Document dest,
        Element parentParent, Element targetParent) throws XPathExpressionException {
        NodeList nl = Tools.evaluateXPath(xpath, source, NS_CONTEXT, XPathConstants.NODESET, NodeList.class);
        LOG.trace(String.format("Found nodes: %s", nl.getLength()));
        if (nl.getLength() == 0) {
            parentParent.removeChild(targetParent);
            return;
        }
        importNodesFromNodeList(dest, targetParent, nl);
    }
    
    private void importNodesFromNodeList(Document doc, Element parentElement, NodeList nodes) {
        for (int i = 0; i < nodes.getLength(); i++) {
            Node importedNode = doc.importNode(nodes.item(i), true);
            parentElement.appendChild(importedNode);
            LOG.trace(String.format("importing node (parent=%s): %s", parentElement.getLocalName(),
                    importedNode.getNodeName()));
        }
    }

    private void insertConfigProfileIntoOrigPom(Document originalPom, Document mojoConfiguration,
            String profileName) throws XPathExpressionException {
        Node profileNode = mojoConfiguration.getFirstChild();
        
        Node idNode = mojoConfiguration.createElementNS(POM_NS_URI, "id");
        idNode.setTextContent(profileName);
        profileNode.insertBefore(idNode, profileNode.getFirstChild());

        Node importedProfileNode = originalPom.importNode(profileNode, true);

        Tools.insertDomNode(originalPom, importedProfileNode, POM_PROFILE_XPATH, NS_CONTEXT);
    }
    
    private void writeIntoPom(File pomFile, String content) throws IOException {
        if (licenseHeaderComment != null) {
            content = addHeader(content);
        }
        FileUtils.writeStringToFile(pomFile, content + "\n");
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
