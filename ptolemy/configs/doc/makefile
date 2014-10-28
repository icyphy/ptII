# Makefile for the Ptolemy runtime configuration documentations
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2000-2014 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY

ME =		ptolemy/configs/doc

# There is no demo directory, put demos in the corresponding domain
# directory so that we can find them if we are looking at domains.
# An alternative would be to split the demo subdirectory into
# directories by domain
DIRS =		#demo

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	doc
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS =

# HTML Files that are derived from other HTML files.
DERIVED_FILES = \
	completeDemosBcvtb.htm \
	completeDemosPtiny.htm \
	completeDemosPtinyKepler.htm \
	demosPtiny.htm \
	demosBcvtb.htm \
	demosPtinyKepler.htm \
	docsBcvtb.htm \
	docsPtdb.htm \
	docsPtiny.htm \
	docsPtinyKepler.htm \
	models.txt

OTHER_FILES_TO_BE_JARED = \
	$(DERIVED_FILES) \
	basicHelp.htm \
	book.htm \
	ClassesIllustrated.png \
	ClassesIllustrated.xml \
	completeDemos.htm \
	copyright.htm \
	demos.htm \
	docs.htm \
	pt_cover_online.png \
	vergilFsmEditorHelp.htm \
	vergilGraphEditorHelp.htm \
	whatsNew.htm \
	whatsNew3.0.2.htm \
	whatsNew4.0.htm \
	whatsNew5.0.htm \
	whatsNew5.1.htm \
	whatsNew6.0.htm \
	whatsNew7.0.htm \
	whatsNew8.0.htm \
	whatsNew10.0.htm \
	whatsNew11.0.htm

EXTRA_SRCS = \
	 \
	ClassesIllustratedVergil.htm \
	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demosModels.txt

JCLASS = $(JSRCS:%.java=%.class)

all: jclass $(DERIVED_FILES)
	@echo "nothing need be done for make all in this directory"

install: all jars
	@echo "nothing need be done for make install in this directory"

# Generate a file that consists of only the text within
# <!--vergilDoc--> ... <!--/vergilDoc--> blocks
completeDemosPtiny.htm: completeDemos.htm
	$(GAWK) -f $(ROOT)/doc/javahtml.awk completeDemos.htm | sed 's/demos.htm/demosPtiny.htm/' > $@  

# Generate a file that consists of only the text within
# <!--ptinyKeplerDocs--> ... <!--/ptinyKeplerDocs--> blocks
completeDemosPtinyKepler.htm: completeDemosPtiny.htm
	$(GAWK) -f $(ROOT)/doc/ptinyKepler.awk completeDemosPtiny.htm | sed 's/demosPtiny.htm/demosPtinyKepler.htm/' > $@  

# Generate a file that consists of only the text within
# <!--bcvtbDocs--> ... <!--/bcvtbDocs--> blocks
completeDemosBcvtb.htm: completeDemosPtiny.htm
	$(GAWK) -f $(ROOT)/doc/ptinyBcvtb.awk completeDemosPtiny.htm | sed 's/demosPtiny.htm/demosPtinyKepler.htm/' > $@  

demosPtiny.htm: demos.htm
	$(GAWK) -f $(ROOT)/doc/javahtml.awk demos.htm | sed 's/completeDemos.htm/completeDemosPtiny.htm/' > $@  

demosBcvtb.htm: demosPtiny.htm
	$(GAWK) -f $(ROOT)/doc/ptinyBcvtb.awk demosPtiny.htm | sed 's/completeDemosPtiny.htm/completeDemosBcvtb.htm/' > $@  

demosPtinyKepler.htm: demosPtiny.htm
	$(GAWK) -f $(ROOT)/doc/ptinyKepler.awk demosPtiny.htm | sed 's/completeDemosPtiny.htm/completeDemosPtinyKepler.htm/' > $@  

docsBcvtb.htm: docsPtiny.htm
	$(GAWK) -f $(ROOT)/doc/ptinyBcvtb.awk docsPtiny.htm | sed 's/introPtiny.htm/intro.htm/' | sed 's/completeDemosPtiny.htm/completeDemosBcvtb.htm/' > $@

docsPtdb.htm: docs.htm
	$(GAWK) -f $(ROOT)/doc/javahtml.awk docs.htm | sed 's/intro.htm/introPtiny.htm/' | sed 's/completeDemos.htm/completeDemosPtiny.htm/' > $@  

docsPtiny.htm: docs.htm
	$(GAWK) -f $(ROOT)/doc/javahtml.awk docs.htm | sed 's/intro.htm/introPtiny.htm/' | sed 's/completeDemos.htm/completeDemosPtiny.htm/' > $@  

docsPtinyKepler.htm: docsPtiny.htm
	$(GAWK) -f $(ROOT)/doc/ptinyKepler.awk docsPtiny.htm | sed 's/introPtiny.htm/introPtinyKepler.htm/' | sed 's/completeDemosPtiny.htm/completeDemosPtinyKepler.htm/' > $@  


# models.txt contains a list of model file names
# Use AUXCLASSPATH so that we work under codecoverage with JavaScope
models.txt: $(ROOT)/ptolemy/actor/gui/HTMLAbout.class completeDemos.htm
	"$(JAVA)" "-Dptolemy.ptII.dir=$(PTII)" \
		-classpath "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)" \
		ptolemy.actor.gui.HTMLAbout 
	# Include the models in the ExecDemos directory.
	ls -1 $(PTII)/ptolemy/demo/ExecDemos/Demos/*.xml | sed "s@$(PTII)@\$$CLASSPATH@" >> models.txt
	LC_ALL=C sort -f models.txt | uniq > /tmp/models.txt
	mv /tmp/models.txt models.txt

demosModels.txt: $(ROOT)/ptolemy/actor/gui/HTMLAbout.class demos.htm models.txt
	"$(JAVA)" "-Dptolemy.ptII.dir=$(PTII)" \
		-classpath "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)" \
		ptolemy.actor.gui.HTMLAbout demos.htm demosModels.txt
	# Include the models in the ExecDemos directory.
	ls -1 $(PTII)/ptolemy/demo/ExecDemos/Demos/*.xml | sed "s@$(PTII)@\$$CLASSPATH@" >> demosModels.txt
	LC_ALL=C sort -f demosModels.txt | uniq > /tmp/demosModels.txt
	mv /tmp/demosModels.txt demosModels.txt

# Check for models in demos.htm that are not in completeDemos.htm
missingDemos: demosModels.txt models.txt
	@sort demosModels.txt > /tmp/demosModels.txt
	@sort models.txt > /tmp/models.txt
	@comm -13 /tmp/models.txt /tmp/demosModels.txt
	@rm -f /tmp/models.txt /tmp/demosModels.txt

update: $(DERIVED_FILES)
	svn commit -m "Master files changed" $(DERIVED_FILES)
	rm -f doc.jar
	$(MAKE) doc.jar
	# The first commit will change completeDemosPtiny.htm, which will cause completeDemosBcvtb.htm to be updated
	svn commit -m "Master files changed" $(DERIVED_FILES)

update_clean: 
	rm -f $(DERIVED_FILES)

check_applets:
	@ls `grep .xml completeDemos.htm | grep -v "<!--" | $(GAWK) -F \" '{print $$2}' | sed 's/.xml/.htm/'` 2>&1 | sed 's/ls: //' | grep "No such file" | $(GAWK) '{print $$1}' | sed 's@:@@'

check_tests:
	@$(ROOT)/adm/bin/chkdemotests 

listDemos:
	@ls `grep .xml completeDemos.htm | grep -v "<!--" | $(GAWK) -F \" '{print $$2}'` | sort

listNewDemos:
	#@ls `grep .xml completeDemos.htm | grep -i "New in Ptolemy II 8.0" | grep -v "<!--" | $(GAWK) -F \" '{print $$2}'`

# Open the demos 10 at a time, print statistics and close.
demosStatistics:
	$(MAKE) --no-print-directory listDemos | xargs -n 10 $(ROOT)/bin/vergil -statistics -exit

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
