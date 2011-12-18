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

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.openengsb.openengsbplugin.tools.Tools;

public class ExtractDocSourceMojoTest extends MojoPreparation {

    @Before
    public void buildInvocationCommand() throws Exception {
        prepareGoal("extractSource");
    }

    @Test
    public void testExtractionOfFile() throws Exception {
        int result =
            Tools.executeProcess(
                Arrays.asList(new String[]{ mvnCommand, "-e", invocation, "-DsourcePath=src/",
                    "-DtargetPath=target/" }),
                new File("src/test/resources/extract"));
        assertEquals(0, result);
        // compare java
        assertEquals(FileUtils.readFileToString(new File("src/test/resources/extract/target/javaout.xml")),
            FileUtils.readFileToString(new File("src/test/resources/extract/javaout.xml")));
        // compare xml
        assertEquals(FileUtils.readFileToString(new File("src/test/resources/extract/target/xmlout.xml")),
            FileUtils.readFileToString(new File("src/test/resources/extract/xmlout.xml")));
        // compare prop
        assertEquals(FileUtils.readFileToString(new File("src/test/resources/extract/target/propout.xml")),
            FileUtils.readFileToString(new File("src/test/resources/extract/propout.xml")));
        // compare cfg
        assertEquals(FileUtils.readFileToString(new File("src/test/resources/extract/target/cfgout.xml")),
            FileUtils.readFileToString(new File("src/test/resources/extract/cfgout.xml")));
    }

}
