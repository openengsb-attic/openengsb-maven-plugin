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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.openengsb.openengsbplugin.exceptions.NoVersionFoundException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/**
 * this class gets the latest version for openengsb-framework from maven central
 */
public class OpenEngSBVersionResolver {

    private String defaultUri;

    public OpenEngSBVersionResolver(String uri) {
        this.defaultUri = uri;
    }

    public OpenEngSBVersionResolver() {
        this.defaultUri =
            "http://search.maven.org/remotecontent?filepath=org/openengsb/framework/openengsb-framework/maven-metadata.xml";
    }


    public String resolveUri() throws NoVersionFoundException {
        try {

            HttpClient client = new DefaultHttpClient();
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            HttpGet httpget = new HttpGet(defaultUri);
            return client.execute(httpget, responseHandler);
        } catch (MalformedURLException e) {
            throw new NoVersionFoundException("Unknown URI", e);
        } catch (IOException e) {
            throw new NoVersionFoundException("IOException caught", e);
        }
    }

    public String resolveVersion(String content) throws NoVersionFoundException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            File tmpFile = createTmpFile(content);
            Document parse = builder.parse(tmpFile.getAbsolutePath());
            NodeList latestNode = parse.getElementsByTagName("latest");
            if (latestNode == null || latestNode.getLength() < 1) {
                throw new NoVersionFoundException("Could not resolve latest version");
            }
            return latestNode.item(0).getFirstChild().getNodeValue();
        } catch (ParserConfigurationException e) {
            throw new NoVersionFoundException("Could not retrieve latest version", e);
        } catch (SAXException e) {
            throw new NoVersionFoundException("Could not retrieve latest version", e);
        } catch (IOException e) {
            throw new NoVersionFoundException("IOException, tmp folder must be writable", e);
        }
    }

    private File createTmpFile(String content) throws IOException {
        File temp = File.createTempFile("metadata", ".xml");
        temp.deleteOnExit();
        BufferedWriter out = new BufferedWriter(new FileWriter(temp));
        out.write(content);
        out.close();
        return temp;
    }

    public String getLatestVersion() throws NoVersionFoundException {
        return resolveVersion(resolveUri());
    }
}
