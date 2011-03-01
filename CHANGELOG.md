openengsb-maven-plugin-1.3.0 2011-04-02
--------------------------------------------

### Highlights
  * prePush mojo
  * checkstyle mojo
  * refactored mojo configuration profile structure so it is easier to write mojos with complex default configuration
  * wrapped mojos are allow to modify the root pom now (see OPENENGSB-938)

### Details
** Bug
    * [OPENENGSB-930] - release mojos leave projects root pom in working copy in modified state
    * [OPENENGSB-951] - Maven Plugin requires nix and win values

** Library Upgrade
    * [OPENENGSB-991] - upgrade to openengsb-root-8

** New Feature
    * [OPENENGSB-506] - include checkstyle in openengsb maven plugin
    * [OPENENGSB-938] - restore pom after executing maven openengsb plugin
    * [OPENENGSB-1012] - prePush mojo

** Task
    * [OPENENGSB-867] - update etc/scripts/* to use the openegsb-maven-plugin -> won't fix
    * [OPENENGSB-935] - change openengsb-maven-plugin project name in pom
    * [OPENENGSB-1017] - add readme and changelog to openengsb-maven-plugin


