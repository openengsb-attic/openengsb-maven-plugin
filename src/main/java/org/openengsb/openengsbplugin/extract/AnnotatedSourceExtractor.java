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
 * Base interface which needs to be implemented to add an additional source type to analyse.
 */
public interface AnnotatedSourceExtractor {

    /**
     * Checks if the extractor can handle a specific file type.
     */
    boolean canExtractFile(File file);

    /**
     * Checks if the line is a start line for the extractor.
     */
    boolean isStartLine(String line);

    /**
     * Checks if the specific line is a stop line for the extractor.
     */
    boolean isStopLine(String line);

    /**
     * Since the lines could be structured quite specific the extrator need to extract the target filename from the line
     * name.
     */
    public String extractTargetFilenameFromLine(String line);

    /**
     * This one is responsible for code highlighting. A java extractor might return java here while a xml extractor
     * return xml here.
     */
    String getLanguage();

}
