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

public class LicenseCheckTest extends MojoPreparation {

    @Before
    public void buildInvocationCommand() throws Exception {
        prepareGoal("licenseCheck");
    }

    @Test
    public void licenseCheckNoLicenseHeader_mojoShouldFail() throws Exception {
        int result = Tools.executeProcess(Arrays.asList(new String[]{ mvnCommand, "-e", invocation }), new File(
                "src/test/resources/licenseCheck/fail"));
        assertEquals(1, result);
    }

    @Test
    public void licenseCheckHeaderAvailable_mojoShouldPass() throws Exception {
        int result = Tools.executeProcess(Arrays.asList(new String[]{ mvnCommand, "-e", invocation }), new File(
                "src/test/resources/licenseCheck/pass"));
        assertEquals(0, result);
    }

    @Test
    public void checkCheckHeadersWithAdditionalIgnores_shouldPass() throws Exception {
        int result = Tools.executeProcess(
                Arrays.asList(new String[] { mvnCommand, "-e", invocation, "-DadditionalExcludes=excludes.txt" }),
                new File("src/test/resources/licenseCheck/fail"));
        assertEquals(0, result);
    }

    @Test
    public void writeHeaderTest() throws Exception {
        File generatedFile = null;
        try {
            File f = new File(ClassLoader.getSystemResource("license/header.txt").toURI());
            String headerStr = FileUtils.readFileToString(f);
            generatedFile = Tools.generateTmpFile(headerStr, ".txt");
            assertEquals(headerStr, FileUtils.readFileToString(generatedFile));
        } finally {
            FileUtils.deleteQuietly(generatedFile);
        }
    }

}
