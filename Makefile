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


#
# azure deployments
#

#put context.xml information under META-INF
az-context = $(shell cp $(WEBAPP_PATH)/src/main/resources/cloud-context.xml $(WEBAPP_PATH)/target/$(WEBAPP_BUILD_NAME)-$(CURRENT_VERSION)/META-INF/context.xml)
#clone the deployment repo
az-clone = git clone --branch master $(1) $(TEMP_LOC)
#copy files to the deployment repo
az-sync = rsync -a --delete $(WEBAPP_PATH)/target/$(WEBAPP_BUILD_NAME)-$(CURRENT_VERSION)/* $(TEMP_LOC)/webapps/$(WEBAPP_TARGET_NAME)
#commit and push the deployment repo
az-commit = cd $(TEMP_LOC) ; git add . ; git commit -a -m "deployment of $(CURRENT_VERSION)" ; git push ; cd $(ORI_LOC) ;
#delete temp checkout
az-cleanup = rm -rf $(TEMP_LOC)


define az-deploy =
	$(call az-context)
	$(call az-clone,$(giturl))
	$(call az-sync)
	$(call az-commit)
	$(call az-cleanup)
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
	$(eval WEBAPP_PATH := configuration/configuration)
	$(eval WEBAPP_BUILD_NAME := configuration)
	$(eval WEBAPP_TARGET_NAME := configuration)
	$(call az-deploy)


