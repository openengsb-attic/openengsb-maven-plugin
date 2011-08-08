openengsb-maven-plugin-1.5.1 2011-08-08
--------------------------------------------

The 1.5.1 release of the OpenEngSB maven plugin fixes a critical windows checkstyle error

Release Notes - OpenEngSB - Version openengsb-maven-plugin-1.5.1

** Bug
    * [OPENENGSB-1850] - in windows checkstyle expects CRLF at end of file

** Library Upgrade
    * [OPENENGSB-1888] - Upgrade openengsb-root to 19
    * [OPENENGSB-1890] - Upgrade commons-compress to 1.2

** Task
    * [OPENENGSB-1817] - Release openengsb-maven-plugin-1.5.1
    * [OPENENGSB-1889] - Adapt name of openengsb-maven-plugin project name


openengsb-maven-plugin-1.5.0 2011-07-13
--------------------------------------------

Although this release does not contain those killer features, compared to 1.4.x it requires various files to be chnaged 
in the sources and does not allow a simply switch.

Release Notes - OpenEngSB - Version openengsb-maven-plugin-1.5.0

** Bug
    * [OPENENGSB-1359] - Missing licenses and checkstyle errors
    * [OPENENGSB-1676] - openengsb:eclipse does not work

** Improvement
    * [OPENENGSB-1330] - update connector mojo in openengsb-maven-plugin 
    * [OPENENGSB-1606] - add module for disabling checkstyle via comments to checkstyle.xml
    * [OPENENGSB-1620] - Also handle domainVersion and domainRange in openengsb-maven-plugin

** Library Upgrade
    * [OPENENGSB-1357] - Upgrade to openengsb-root-14
    * [OPENENGSB-1623] - Upgrade to openengsb-root-17
    * [OPENENGSB-1624] - Upgrade pax-runner-platform to 1.7.3
    * [OPENENGSB-1681] - Upgrade maven-license-plugin to 1.9.0
    * [OPENENGSB-1723] - Upgrade pax runner to 1.7.4

** Task
    * [OPENENGSB-1302] - Release openengsb-maven-plugin-1.5.0
    * [OPENENGSB-1650] - Checkstyle rule for newline at end of file
    * [OPENENGSB-1664] - replace openengsb:provision with run2.sh
    * [OPENENGSB-1753] - Disable openengsb:provision until Java 1.7 is available


openengsb-maven-plugin-1.4.0 2011-04-18
--------------------------------------------

The remainging things done via configurations (such as docs) are now moved from the OpenEngSB into the openengsb-maven-plugin.
In addition varous bugs and minor problems had been fixed.

### Highlights
  * upgrade entire structure to m3 (OPENENGSB-1296 and OPENENGSB-1297)
  * docs entirely like the OpenEngSB could be build without really configuring docs if the structure is exactly the same
  * a client archetype can be build directly using the plugin

### Details
** Bug
    * [OPENENGSB-1073] - openengsb:provision deletes data
    * [OPENENGSB-1109] - eclipse checkstyle-plugin gets really slow when offline
    * [OPENENGSB-1161] - openengsb:provision broken (on unix)
    * [OPENENGSB-1299] - Plugin cannot run prePush goal because of DOM not-found exception

** Improvement
    * [OPENENGSB-1121] - change exception handling in checkstyle
    * [OPENENGSB-1294] - Remove cross dependencies back to openengsb

** Library Upgrade
    * [OPENENGSB-1295] - Upgrade to openengsb-root 13
    * [OPENENGSB-1296] - Upgrade maven-plugin-api to 3.0.3
    * [OPENENGSB-1297] - Upgrade maven-core to 3.0.3
    * [OPENENGSB-1298] - Upgrade log4j to 1.2.16

** New Feature
    * [OPENENGSB-504] - use convention over configuration for docs in openengsb maven plugin
    * [OPENENGSB-1054] - Overwrite license-plugin ignore settings during configuring openengsb-mavenplugin
    * [OPENENGSB-1280] - Add client archetype to openengsb tool suite plugin

** Task
    * [OPENENGSB-1058] - release openengsb-maven-plugin-1.4.0
    * [OPENENGSB-1066] - Add openengsb-maven-plugin to openengsb-root
    * [OPENENGSB-1067] - errors in the license
    * [OPENENGSB-1300] - Cleanup code to openengsb formatting rules

