openengsb-maven-plugin-1.3.2 2011-03-22
--------------------------------------------

some minor fixes

### Highlights

### Details
** Bug
    * [OPENENGSB-1109] - eclipse checkstyle-plugin gets really slow when offline
    * [OPENENGSB-1119] - Eclipse Checkstyle configuration has a bug

** Improvement
    * [OPENENGSB-1121] - change exception handling in checkstyle

** Task
    * [OPENENGSB-1149] - release openengsb-maven-plugin-1.3.2


openengsb-maven-plugin-1.3.1 2011-03-08
--------------------------------------------

Some small but important fixes

### Highlights

### Details
** Bug
    * [OPENENGSB-1073] - openengsb:provision deletes data

** Task
    * [OPENENGSB-1064] - Release openengsb-maven-plugin-1.3.1
    * [OPENENGSB-1066] - Add openengsb-maven-plugin to openengsb-root
    * [OPENENGSB-1067] - errors in the license


openengsb-maven-plugin-1.3.0 2011-03-04
--------------------------------------------

Most of the things which were done via etc/scripts/* in the OpenEngSB are now possible via the openengsb-maven-plugin now. Many useful default
configurations (plugin configurations from various poms) have been extracted from the OpenEngSB and are now shipped with the
openengsb-maven-plugin so that the configuration effort for related projects is reduced. 

### Highlights
  * prePush mojo
  * checkstyle mojo
  * eclipse mojo
  * refactored mojo configuration profile structure so it is easier to write mojos with complex default configuration
  * wrapped mojos are allowed to modify the root pom now (see OPENENGSB-938)

### Details
** Bug
    * [OPENENGSB-930] - release mojos leave projects root pom in working copy in modified state
    * [OPENENGSB-951] - Maven Plugin requires nix and win values

** Library Upgrade
    * [OPENENGSB-991] - upgrade to openengsb-root-8

** New Feature
    * [OPENENGSB-506] - include checkstyle in openengsb maven plugin
    * [OPENENGSB-759] - Set wicket in debug mode if started via openengsb:pluginsuite
    * [OPENENGSB-938] - restore pom after executing maven openengsb plugin
    * [OPENENGSB-1012] - prePush mojo

** Task
    * [OPENENGSB-935] - change openengsb-maven-plugin project name in pom
    * [OPENENGSB-990] - change license in all files to "vereinslicense"
    * [OPENENGSB-1017] - add readme and changelog to openengsb-maven-plugin
    * [OPENENGSB-1020] - openengsb:eclipse goal using checkstyle
    * [OPENENGSB-1028] - update default license header template
    * [OPENENGSB-1038] - Move maven notice plugin version to openengsb-root
    * [OPENENGSB-1055] - license mojo ignore *.config in docs/example folder
    * [OPENENGSB-1057] - releasen openengsb-maven-plugin-1.3.0

