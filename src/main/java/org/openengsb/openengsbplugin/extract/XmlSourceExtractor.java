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

package org.openengsb.openengsbplugin.extract;

import java.io.File;

/**
 * Extractor able to find extractions from xml source files.
 */
public class XmlSourceExtractor implements AnnotatedSourceExtractor {

    private static final String FILE_ENDING = ".xml";
    private static final String START_ANNOTATION = "<!-- @extract-start";
    private static final String PART_TO_REMOVE = "-->";
    private static final String END_ANNOTATION = "<!-- @extract-end -->";
    private static final String HIGHLIGHT = "xml";

    @Override
    public boolean canExtractFile(File file) {
        return file.getName().endsWith(FILE_ENDING);
    }

    @Override
    public boolean isStartLine(String line) {
        return line.contains(END_ANNOTATION);
    }

    @Override
    public boolean isStopLine(String line) {
        return line.contains(START_ANNOTATION);
    }

    @Override
    public String extractTargetFilenameFromLine(String line) {
        String nameWithEnding = line.trim().substring(START_ANNOTATION.length() + 1);
        int toRemove = nameWithEnding.indexOf(PART_TO_REMOVE);
        return nameWithEnding.substring(0, toRemove).trim();
    }

    @Override
    public String getLanguage() {
        return HIGHLIGHT;
    }

}
