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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.openengsb.openengsbplugin.extract.AnnotatedSourceExtractor;
import org.openengsb.openengsbplugin.extract.OpenEngSBMavenPluginJavaSourceExtractor;
import org.openengsb.openengsbplugin.extract.OpenEngSBMavenPluginPropSourceExtractor;
import org.openengsb.openengsbplugin.extract.OpenEngSBMavenPluginXmlSourceExtractor;

import com.google.common.collect.Lists;

/**
 * Executes a simple script extracting sources to docbook based on annotations used in the src.
 *
 * @goal extractSource
 *
 * @inheritedByDefault false
 *
 * @requiresProject true
 *
 * @aggregator true
 */
public class ExtractDocSourceMojo extends AbstractMojo {

    /**
     * The (relative) path to the script to be run for provisioning
     *
     * @parameter expression="${sourcePath}"
     */
    private File sourcePath;

    /**
     * The (relative) path to the script to be run for provisioning
     *
     * @parameter expression="${targetPath}"
     */
    private File targetPath;

    /**
     * All extractors which should be searched for the possibility to extract source.
     */
    private List<AnnotatedSourceExtractor> sourceExtractors = Lists.newArrayList(
        new OpenEngSBMavenPluginJavaSourceExtractor(),
        new OpenEngSBMavenPluginXmlSourceExtractor(), new OpenEngSBMavenPluginPropSourceExtractor());

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            targetPath.mkdirs();
            extractCode(sourcePath);
        } catch (Exception e) {
            throw new MojoExecutionException("Source couldn't be extracted", e);
        }
    }

    private void extractCode(File toSearch) throws Exception {
        File[] elements = toSearch.listFiles();
        for (int i = 0; i < elements.length; ++i) {
            File file = elements[i];
            if (file.isDirectory()) {
                extractCode(file);
            } else {
                AnnotatedSourceExtractor extractor = findSourceExtractor(file);
                if (extractor == null) {
                    continue;
                }
                extractAnnotatedCode(file, extractor);
            }
        }
    }

    private AnnotatedSourceExtractor findSourceExtractor(File file) {
        for (AnnotatedSourceExtractor extractor : sourceExtractors) {
            if (extractor.canExtractFile(file)) {
                return extractor;
            }
        }
        return null;
    }

    private void extractAnnotatedCode(File file, AnnotatedSourceExtractor extractor) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            new FileInputStream(file)));
        String line = null;
        boolean extract = false;
        BufferedWriter writer = null;
        while ((line = reader.readLine()) != null) {
            if (extract) {
                if (extractor.isStopLine(line)) {
                    closeFile(writer);
                    extract = false;
                    writer = null;
                    continue;
                } else {
                    writer.append(line);
                    writer.newLine();
                }
            } else {
                if (extractor.isStartLine(line)) {
                    String name = extractor.extractTargetFilenameFromLine(line);
                    if (StringUtils.isEmpty(name)) {
                        continue;
                    }
                    extract = true;
                    writer = createXiIncludeFile(name, extractor.getLanguage());
                }
            }
        }
        if (writer != null) {
            closeFile(writer);
        }
    }

    private BufferedWriter createXiIncludeFile(String file, String language) throws Exception {
        targetPath.mkdirs();
        File targetFile = new File(targetPath, file + ".xml");
        BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile));
        writer.newLine();
        writer
            .write("<para xmlns=\"http://docbook.org/ns/docbook\" xmlns:xi=\"http://www.w3.org/2001/XInclude\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" version=\"5.0\">");
        writer.newLine();
        writer.write("<programlisting language=\"" + language + "\"><![CDATA[");
        writer.newLine();
        return writer;
    }

    private void closeFile(BufferedWriter writer) throws Exception {
        writer.write("]]>");
        writer.newLine();
        writer.write("</programlisting>");
        writer.newLine();
        writer.write("</para>");
        writer.flush();
        writer.close();
    }

}
