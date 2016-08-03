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
pwsample: sample
	rm -rf /opt/tomcat/webapps/test{,.war}
	rsync --partial guice/sample-rest-service/target/*.war /opt/tomcat/webapps/test.war

sample: clean
	$(MVN) package -DskipTests=true -am --projects guice/sample-rest-service

config-service-full: clean
	$(MVN) package -DskipTests=true -am --projects configuration/configuration
	rsync -avzr --progress configuration/configuration/target/*.war /opt/tomcat/webapps/config.war

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
