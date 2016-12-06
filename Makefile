#
CURRENT_VERSION=$(shell grep version pom.xml | head -n2 | tail -n1 | tr -d ' ' | tr '<>' '|' | cut -d'|' -f3)



# Set up maven binary, also an alias for skipTests.

notest=false
MAVEN_PARALLELISM=4

ifneq ($(notest), false)
	MVN=mvn3 -T$(MAVEN_PARALLELISM) -DskipTests
else
	MVN=mvn3 -T$(MAVEN_PARALLELISM)
endif

ifeq ($(env), azure)
	MVN=mvn3 -T$(MAVEN_PARALLELISM) -P azure
endif

RSYNC=rsync --progress -vzr

ifeq ($(env), azure)
	HOST=$(host)
	DESTINATION=/opt/tomcat/
	WEBAPPS=$(HOST):$(DESTINATION)core-services/webapps
	RSYNC=rsync --progress -vzr --chmod=a+wrx --perms
endif

all: install

hagent:
	$(MVN) package -am --projects service-manager/host-agent

sman:
	$(MVN) package -am --projects service-manager/service-manager

sman-full: sman
ifndef host
		$(error host is not set)
endif
	 $(RSYNC) service-manager/configuration/target/*.war $(WEBAPPS)/configuration.war
	 $(RSYNC) user-manager/service/target/*.war $(WEBAPPS)/user-manager.war
	 $(RSYNC) service-manager/service-manager/target/*.war $(WEBAPPS)/service-manager.war


#
#
# PW Targets
#
#

pwconfig-service-full:
	$(MVN) package -DskipTests=true -am --projects service-manager/configuration
	rsync -avzr --progress service-manager/configuration/target/*.war /opt/tomcat/webapps/config.war

pwuman:
	$(MVN) package -DskipTests=true -am --projects user-manager/service
	rsync -avzr --progress user-manager/service/target/*.war /opt/tomcat/webapps/user-manager.war

pwsman:
	$(MVN) package -DskipTests=true -am --projects service-manager/service-manager
	rsync -avzr --progress service-manager/service-manager/target/*.war /opt/tomcat/webapps/service-manager.war	

pwsmtail:
	rsync -avzr --progress service-manager/service-manager/src/main/webapp/vendor/logui-SNAPSHOT/* /opt/tomcat/webapps/service-manager/vendor/logui-SNAPSHOT/
	rsync -avzr --progress service-manager/service-manager/src/main/webapp/WEB-INF/template/* /opt/tomcat/webapps/service-manager/WEB-INF/template/

pwhagent: hagent
	rsync -avzr --progress service-manager/host-agent/target/*.war /opt/tomcat/webapps/host-agent.war

#
#
# Code Generation
#
#
colf:
	-rm guice/common/src/main/java/com/peterphi/std/guice/common/logging/logreport/*.java
	colf -b guice/common/src/main/java/ -p com/peterphi/std/guice/common/logging java guice/common/src/main/colfer/logreport.colf

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

