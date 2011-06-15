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

package org.openengsb.openengsbplugin.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

public abstract class Tools {

    private static final Logger LOG = Logger.getLogger(Tools.class);

    private Tools() {
    }

    public static String capitalizeFirst(String st) {
        if (st == null) {
            return null;
        } else if (st.matches("[\\s]*")) {
            return st;
        } else if (st.length() == 1) {
            return st.toUpperCase();
        } else {
            return st.substring(0, 1).toUpperCase() + st.substring(1, st.length());
        }
    }

    public static void replaceInFile(File f, String pattern, String replacement) throws IOException {

        String str = FileUtils.readFileToString(f).replaceAll(pattern, replacement);

        FileUtils.writeStringToFile(f, str);

    }

    /**
     * Renames <code>&lt;module&gt;oldStr&lt;/module&gt;</code> to <code>&lt;module&gt;newStr&lt;/module&gt;</code>
     */
    public static void renameSubmoduleInPom(String oldStr, String newStr) throws MojoExecutionException {
        try {
            File pomFile = new File("pom.xml");
            if (pomFile.exists()) {
                Tools.replaceInFile(pomFile, String.format("<module>%s</module>", oldStr),
                        String.format("<module>%s</module>", newStr));
            }
        } catch (Exception e) {
            throw new MojoExecutionException("Couldn't modifiy module entry in pom file!");
        }
    }

    public static String readValueFromStdin(Scanner sc, String name, String defaultvalue) {
        System.out.print(String.format("%s [%s]: ", name, defaultvalue));
        String line = sc.nextLine();
        if (line == null || line.matches("[\\s]*")) {
            return defaultvalue;
        }
        return line;
    }

    public static void renameArtifactFolderAndUpdateParentPom(String oldFileName, String newFileName)
        throws MojoExecutionException {
        File from = new File(oldFileName);
        System.out.println(String.format("\"%s\" exists: %s", oldFileName, from.exists()));
        if (from.exists()) {
            System.out.println(String.format("Trying to rename to: \"%s\"", newFileName));
            File to = new File(newFileName);
            boolean success = false;
            if (!to.exists()) {
                success = from.renameTo(to);
                System.out.println("renamed successfully");
                Tools.renameSubmoduleInPom(oldFileName, newFileName);
            }
            if (!success) {
                throw new MojoExecutionException("Couldn't rename!");
            }
        } else {
            throw new MojoExecutionException("Artifact wasn't created as expected!");
        }
    }

    /**
     * Parses an XML document from {@code str} (defaults to namespaceaware = true)
     *
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     */
    public static Document parseXMLFromString(String str) throws ParserConfigurationException, SAXException,
            IOException {
        return parseXMLFromString(str, true);
    }

    /**
     * Parses an XML document from {@code str}
     */
    public static Document parseXMLFromString(String str, boolean namespaceaware) throws ParserConfigurationException,
            SAXException, IOException {
        StringReader sr = null;
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(namespaceaware);
            DocumentBuilder db = dbf.newDocumentBuilder();
            sr = new StringReader(str);
            return db.parse(new InputSource(sr));
        } finally {
            IOUtils.closeQuietly(sr);
        }
    }

    public static <T> T evaluateXPath(String xpathStr, Document doc, NamespaceContext nsContext, QName returnTypeQName,
            Class<T> returnType) throws XPathExpressionException {
        LOG.debug(String.format("evaluating xpath: %s", xpathStr));
        XPath xpath = XPathFactory.newInstance().newXPath();
        if (nsContext != null) {
            xpath.setNamespaceContext(nsContext);
        }
        return returnType.cast(xpath.evaluate(xpathStr, doc, returnTypeQName));
    }

    public static File generateTmpFile(String content, String suffix) throws IOException {
        return generateTmpFileInDirectory(content, suffix, null);
    }

    public static File generateTmpFileInDirectory(String content, String suffix, File directory) throws IOException {
        File f = File.createTempFile(UUID.randomUUID().toString(), suffix, directory);
        FileUtils.writeStringToFile(f, content);
        LOG.debug(String.format("generated file: %s", f.toURI().toString()));
        return f;
    }

    public static String serializeXML(Document doc) throws IOException {
        StringWriter sw = null;
        try {
            sw = new StringWriter();
            XMLSerializer xmlSerializer = new XMLSerializer();
            OutputFormat of = new OutputFormat(doc, doc.getXmlEncoding(), false);
            of.setStandalone(doc.getXmlStandalone());
            xmlSerializer.setOutputFormat(of);
            xmlSerializer.setOutputCharStream(sw);
            xmlSerializer.serialize(doc);
            return sw.toString();
        } finally {
            IOUtils.closeQuietly(sw);
        }
    }

    /**
     * Insert dom node into {@code parentDoc} at the given {@code xpath} (if this path doesnt exist, the elements are
     * created). Note: text content of nodes and attributes aren't considered.
     */
    public static void insertDomNode(Document parentDoc, Node nodeToInsert, String xpath, NamespaceContext nsContext)
        throws XPathExpressionException {
        LOG.trace("insertDomNode() - start");
        String[] tokens = xpath.split("/");
        String currPath = "";
        Node parent = null;
        for (int i = 0; i < tokens.length; i++) {
            if (tokens[i].matches("[\\s]*")) {
                continue;
            }
            LOG.trace(String.format("parent = %s", parent == null ? "null" : parent.getLocalName()));
            currPath += "/" + tokens[i];
            Node result = Tools.evaluateXPath(currPath, parentDoc, nsContext, XPathConstants.NODE, Node.class);
            LOG.trace(String.format("result empty: %s", result == null));
            if (result == null) {
                String elemName = null;
                // attribute filter
                elemName = tokens[i].replaceAll("\\[.*\\]", "");

                Element element = null;

                if (elemName.contains(":")) {
                    String[] elemenNameTokens = elemName.split(":");
                    String prefix = elemenNameTokens[0];
                    elemName = elemenNameTokens[1];
                    element = parentDoc.createElementNS(nsContext.getNamespaceURI(prefix), elemName);
                } else {
                    element = parentDoc.createElement(elemName);
                }
                LOG.trace(String.format("elementName: %s", elemName));
                parent.appendChild(element);
                result = element;
            }
            parent = result;
        }
        LOG.trace("finally inserting the node..");
        LOG.trace(String.format("parent node: %s", parent == null ? "null" : parent.getLocalName()));
        LOG.trace(String.format("node to insert = %s", nodeToInsert == null ? "null" : nodeToInsert.getLocalName()));
        parent.appendChild(nodeToInsert);
        LOG.trace("insertDomNode() - end");
    }

    public static int executeProcess(List<String> command, File targetDirectory) throws IOException,
            InterruptedException {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(targetDirectory);
        if (targetDirectory != null) {
            LOG.trace(String.format("processBuilder.directory().exists(): %s", processBuilder.directory().exists()));
        }
        Process p = processBuilder.start();

        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = br.readLine()) != null) {
                LOG.debug(line);
            }
        } finally {
            IOUtils.closeQuietly(br);
        }

        p.waitFor();
        return p.exitValue();
    }

    /**
     * Remove node with given {@code xpath} from {@code targetDocument}. When {@code removeParent} is set to
     * {@code true} the parent node will also be removed if the removed node was the only child. The method returns
     * {@code true} if the node specified by {@code xpath} has been found and successfully removed from its parent node.
     */
    public static boolean removeNode(String xpath, Document targetDocument, NamespaceContext nsContext,
            boolean removeParent) throws XPathExpressionException {
        Node nodeToRemove = evaluateXPath(xpath, targetDocument, nsContext, XPathConstants.NODE, Node.class);
        if (nodeToRemove == null) {
            return false;
        }
        Node parent = nodeToRemove.getParentNode();
        if (parent == null) {
            return false;
        }
        parent.removeChild(nodeToRemove);
        if (removeParent && parent.getChildNodes().getLength() == 0) {
            Node parentParent = parent.getParentNode();
            if (parentParent != null) {
                parentParent.removeChild(parent);
            }
        }
        return true;
    }

    public static Document newDOM() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        return db.newDocument();
    }

}
