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

#
#
# PW Targets
#
#

config-service-full:
	$(MVN) package -DskipTests=true -am --projects service-manager/configuration
	rsync -avzr --progress service-manager/configuration/target/*.war /opt/tomcat/webapps/config.war

uman:
	$(MVN) package -DskipTests=true -am --projects user-manager/service
	rsync -avzr --progress user-manager/service/target/*.war /opt/tomcat/webapps/user-manager.war

sman:
	$(MVN) package -DskipTests=true -am --projects service-manager/service-manager
	rsync -avzr --progress service-manager/service-manager/target/*.war /opt/tomcat/webapps/service-manager.war	
smtail:
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


#
# azure deployments
#

#clone the deployment repo
az-clone = git clone --branch master $(1) $(TEMP_LOC)
#pull on an existing checkout
az-pull = cd $(TEMP_LOC) ; git pull ; cd $(ORI_LOC) ;
#copy files to the deployment repo
az-sync = rsync -a --delete $(WEBAPP_PATH)/target/$(WEBAPP_BUILD_NAME)-$(CURRENT_VERSION).war $(TEMP_LOC)/webapps/$(WEBAPP_TARGET_NAME).war
#add or remove files
az-addrm = cd $(TEMP_LOC) ; git rm -r webapps/$(WEBAPP_TARGET_NAME) ; git add webapps/$(WEBAPP_TARGET_NAME).war ; cd $(ORI_LOC) ;
#commit and push the deployment repo
az-commit = cd $(TEMP_LOC) ; git commit -a -m "deployment of $(CURRENT_VERSION)" ; git push  ; cd $(ORI_LOC) ;
#delete temp checkout
az-cleanup = rm -rf $(TEMP_LOC)


define az-deploy =
ifndef CO_PATH
	$(call az-clone,$(giturl))
endif
ifdef CO_PATH
	$(eval TEMP_LOC := ${CO_PATH})
	$(call az-pull)
endif
	$(call az-sync)
	$(call az-addrm)
	$(call az-commit)
ifndef CO_PATH
	$(call az-cleanup)
endif
endef

azurls:
#verfiy git deploy url is present
ifndef giturl
		$(error giturl is not set)
endif

azlocs:
	$(eval TEMP_LOC := ${TEMP}/$(shell uuidgen))
	$(eval ORI_LOC := $(shell pwd))

configservce-az: azurls azlocs package
	$(eval WEBAPP_PATH := service-manager/configuration)
	$(eval WEBAPP_BUILD_NAME := configuration)
	$(eval WEBAPP_TARGET_NAME := configuration)
	$(call az-deploy)

usermanager-az: azurls azlocs package
	$(eval WEBAPP_PATH := user-manager/service)
	$(eval WEBAPP_BUILD_NAME := user-manager)
	$(eval WEBAPP_TARGET_NAME := user-manager)
	$(call az-deploy)

servicemanager-az: azurls azlocs package
	$(eval WEBAPP_PATH := service-manager/service-manager)
	$(eval WEBAPP_BUILD_NAME := service-manager)
	$(eval WEBAPP_TARGET_NAME := service-manager)
	$(call az-deploy)

sm-az: azurls azlocs package
ifndef CO_PATH
	$(call az-clone,$(giturl))
endif
ifdef CO_PATH
	$(eval TEMP_LOC := ${CO_PATH})
	$(call az-pull)
endif
	$(eval WEBAPP_PATH := service-manager/configuration)
	$(eval WEBAPP_BUILD_NAME := configuration)
	$(eval WEBAPP_TARGET_NAME := configuration)
	$(call az-sync)
	$(call az-addrm)
	$(eval WEBAPP_PATH := user-manager/service)
	$(eval WEBAPP_BUILD_NAME := user-manager)
	$(eval WEBAPP_TARGET_NAME := user-manager)
	$(call az-sync)
	$(call az-addrm)
	$(eval WEBAPP_PATH := service-manager/service-manager)
	$(eval WEBAPP_BUILD_NAME := service-manager)
	$(eval WEBAPP_TARGET_NAME := service-manager)
	$(call az-sync)
	$(call az-addrm)
	$(call az-commit)
ifndef CO_PATH
	$(call az-cleanup)
endif


sm-full: package
ifndef host
		$(error host is not set)
endif
	 $(RSYNC) service-manager/configuration/target/*.war $(WEBAPPS)/configuration.war
	 $(RSYNC) user-manager/service/target/*.war $(WEBAPPS)/user-manager.war
	 $(RSYNC) service-manager/service-manager/target/*.war $(WEBAPPS)/service-manager.war


