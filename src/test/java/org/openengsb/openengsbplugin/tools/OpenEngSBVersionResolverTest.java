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

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;
import org.openengsb.openengsbplugin.exceptions.NoVersionFoundException;

public class OpenEngSBVersionResolverTest {

    private final String validUri =
        "http://search.maven.org/remotecontent?filepath=org/openengsb/framework/openengsb-framework/maven-metadata.xml";
    private final String invalidUri =
        "http://search.maven.org/remotecontent?filepath=org/openengsb/framework/openengsb-framework/metadata.xml";

    private final String validMetadata = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<metadata>\n" +
        "  <groupId>org.openengsb.framework</groupId>\n" +
        "  <artifactId>openengsb-framework</artifactId>\n" +
        "  <versioning>\n" +
        "    <latest>2.1.0</latest>\n" +
        "    <release>2.1.0</release>\n" +
        "    <versions>\n" +
        "      <version>2.0.4</version>\n" +
        "      <version>2.1.0</version>\n" +
        "    </versions>\n" +
        "    <lastUpdated>20111021082107</lastUpdated>\n" +
        "  </versioning>\n" +
        "</metadata>";

    private final String invalidMetadata = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<metadata>\n" +
        "  <groupId>org.openengsb.framework</groupId>\n" +
        "  <artifactId>openengsb-framework</artifactId>\n" +
        "</metadata>";

    @Test
    public void testResolveUri() throws Exception {
        OpenEngSBVersionResolver resolver = new OpenEngSBVersionResolver(validUri);
        String s = resolver.resolveUri();
        assertTrue(s.startsWith(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<metadata>\n  <groupId>org.openengsb.framework</groupId>\n  <artifactId>openengsb-framework</artifactId>\n  <versioning>\n"));
    }

    @Test(expected = NoVersionFoundException.class)
    public void testResolveInvalidUri_ShouldThrowException() throws Exception {
        OpenEngSBVersionResolver resolver = new OpenEngSBVersionResolver(invalidUri);
        String s = resolver.resolveUri();
        assertTrue(s.startsWith(
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<metadata>\n  <groupId>org.openengsb.framework</groupId>\n  <artifactId>openengsb-framework</artifactId>\n  <versioning>\n"));
    }

    @Test
    public void testRetrieveVersion() throws Exception {
        OpenEngSBVersionResolver resolver = new OpenEngSBVersionResolver();
        String s = resolver.resolveVersion(validMetadata);
        assertThat(s, equalTo("2.1.0"));
    }


    @Test(expected = NoVersionFoundException.class)
    public void testRetrieveVersionWithInvalidContent_ShouldThrowException() throws Exception {
        OpenEngSBVersionResolver resolver = new OpenEngSBVersionResolver();
        resolver.resolveVersion(invalidMetadata);
    }
}
