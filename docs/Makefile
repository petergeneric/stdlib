# Minimal makefile for Sphinx documentation
#

include Makefile.local

# You can set these variables from the command line.
SPHINXOPTS    =
SPHINXBUILD   = sphinx-build
#SPHINXBUILD   = sphinx-build
SOURCEDIR     = .
BUILDDIR      = _build

version=latest
branch=$(shell git rev-parse --abbrev-ref HEAD)

all:
	@cd ..
	@$(SPHINXBUILD) -M html "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS)
	
help:
	@$(SPHINXBUILD) -M help "$(SOURCEDIR)" "$(BUILDDIR)" $(SPHINXOPTS) $(O)

upload: all
	rsync -avzr --partial --progress --delete --exclude=_sources _build/html/* $(DOCUMENTATION_FOLDER)/$(version)/
	rsync -avzr --partial --progress --delete --exclude=_sources _build/html/* $(DOCUMENTATION_FOLDER)/$(branch)/

clean:
	-rm -rf _build
#	@cd figures && make clean