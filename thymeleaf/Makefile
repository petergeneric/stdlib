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


all: install


#
# Local Targets
#
-include Makefile.local


#
#
# Standard Maven targets
#
#

compile:
	$(MVN) clean compile

dependencies:
	$(MVN) install -DskipTests
	$(MVN) dependency:tree

package:
	$(MVN) clean package

test:
	$(MVN) clean test

install:
	$(MVN) clean install

clean:
	$(MVN) clean

release:
	env MVN=mvn3 ./release.sh

