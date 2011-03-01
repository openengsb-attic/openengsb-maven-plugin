====
    Copyright 2010 OpenEngSB Division, Vienna University of Technology

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
====

The openengsb-maven-plugin is a plugin for Apache Maven, intended to simplify various activities (creating domains or connectors, building a release artifact of the whole project etc.) when developing based on the OpenEngSB.

Setup Project and Kick Off
==========================
Execute "mvn install"

Now run "mvn eclipse:eclipse" and import the projects into eclipse.

Full Test
---------
Before creating a pull request, run the following command:

mvn openengsb:prePush

Further Information
-------------------
This readme gives only the most important information for developers. General information about this project is located at http://openengsb.org.
The detailed developer and user documentation is located at file:///win/d/cd_flex/openengsb/docs/manual/target/manual/html-single/openengsb-manual.html#developer.quickstart.tools.mavenplugin.

