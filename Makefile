#


# Set up maven binary, also an alias for skipTests.

notest=false
MAVEN_PARALLELISM=4

ifneq ($(notest), false)
	MVN=mvn3 -T$(MAVEN_PARALLELISM) -DskipTests
else
	MVN=mvn3 -T$(MAVEN_PARALLELISM)
endif

all: install


#
#
# PW Targets
#
#

config-service-full:
	$(MVN) package -DskipTests=true -am --projects configuration/configuration
	rsync -avzr --progress configuration/configuration/target/*.war /opt/tomcat/webapps/config.war

uman:
	$(MVN) package -DskipTests=true -am --projects user-manager/service
	rsync -avzr --progress user-manager/service/target/*.war /opt/tomcat/webapps/user-manager.war

#
#
# Standard Maven targets
#
#

compile:
	$(MVN) clean compile

dependencies:
	$(MVN) clean dependency:tree

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
	env MVN=mvn3 ./release.sh
