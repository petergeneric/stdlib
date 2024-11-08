#
CURRENT_VERSION=$(shell grep version pom.xml | head -n2 | tail -n1 | tr -d ' ' | tr '<>' '|' | cut -d'|' -f3)



# Set up maven binary, also an alias for skipTests.

notest=false
MAVEN_PARALLELISM=4

ifneq ($(notest), false)
	MVN=mvn -T$(MAVEN_PARALLELISM) -DskipTests
else
	MVN=mvn -T$(MAVEN_PARALLELISM)
endif

RSYNC=rsync --progress -vzr

all: install

uman: clean
	$(MVN) package -DskipTests=true -am --projects user-manager/service

uman-deploy:
	 $(RSYNC) user-manager/service/target/*.war $(WEBAPPS)/user-manager.war

uman-full: uman uman-deploy


#
# Local Targets
#
-include Makefile.local

#
#
# Code Generation
#
#

#
#
# Standard Maven targets
#
#

compile:
	$(MVN) clean compile

dependencies:
	$(MVN) install -DskipTests
	mvn dependency:tree

package:
	$(MVN) clean package

test:
	$(MVN) clean test

install:
	$(MVN) clean install

eclipse:
	$(MVN) eclipse:clean eclipse:eclipse -DdownloadSources=true -DdownloadJavadocs=true

clean:
	$(MVN) clean

release:
	env MVN=mvn ./release.sh

