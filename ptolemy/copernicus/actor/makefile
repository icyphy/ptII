# Makefile for Ptolemy II Java deep code generation
#
# @Authors: Steve Neuendorffer, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998-2003 The Regents of the University of California.
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

ME =		ptolemy/copernicus/actor

DIRS =		test

# Root of the Java directory
ROOT =		../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	actor
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	Main.java 

OTHER_FILES_TO_BE_JARED = \
	runCommandTemplate.txt

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) 

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc 

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demo, run 'make demo'"

install: jclass $(PTCLASSJAR)

# Sinewave
MODEL =			Sinewave
MODELPATH =		$(ROOT)/ptolemy/actor/lib/$(MODEL).xml

# Location where we create code that we are generating
TARGETPACKAGE = 	ptolemy.copernicus.actor.cg.$(MODEL)
TARGETPATH =		ptolemy/copernicus/actor/cg/$(MODEL)

TARGETCLASS = $(TARGETPACKAGE).$(MODEL)

JVMSIZE = -Xmx256m

# Soot command line arguments for the user.  To list all the arguments, use
# make COMPILEOPTIONS=-h
COMPILEOPTIONS=

# Time that we should let code generation run before exiting in ms.
WATCHDOGTIMEOUT =     600000
# Number of Iterations to run the generated code for.
ITERATIONS =		1000
ITERATIONSPARAMETER =	,iterations:$(ITERATIONS)

demo:
	@echo "Run 'make demo1' to generate the Sinewave actor in ./cg/"
	@echo "Run 'make demo2' to generate the Sinewave actor in \$$PTII/ptolemy/actor"
	@echo "
demo1:
	@echo "generate actor code for $(MODELPATH) in $(TARGETPATH)"
	$(ROOT)/bin/copernicus -codeGenerator actor \
		-verbose \
		-codeGenerator   "actor" \
		-compile	 true \
		-iterations      "$(ITERATIONS)" \
		-iterationsParameter "$(ITERATIONSPARAMETER)" \
		-java            "`echo "$(JAVA)" | sed 's@/cygdrive/\([a-z]\)@\1:@'`" \
		-jvmSize         "$(JVMSIZE)" \
		-run		 true \
		-ptIIUserDirectory  "`echo "$(PTII)" | sed 's@\\\@/@g'`" \
		-targetPackage   "$(TARGETPACKAGE)" \
		-targetPath      "$(TARGETPATH)" \
		-watchDogTimeout "$(WATCHDOGTIMEOUT)" \
		$(MODELPATH)

demo2:
	@echo "Generate a .class file version of the Sinewave composite"
	@echo "actor in $(PTII)/ptolemy/actor/lib"
	$(ROOT)/bin/copernicus -codeGenerator actor \
		-targetPackage ptolemy.actor.lib \
		-targetPath "." $(PTII)/ptolemy/actor/lib/Sinewave.xml
	@echo "##################################################"
	@echo "Now, because of the way the MoMLParser is designed, "
	@echo "Sinewave.class is loaded before Sinewave.xml..."
	@echo "This automatically generated class behaves almost exactly"
	@echo "the same as the moml loaded class.."
	@echo "You can, for instance open actor/lib/test/auto/Sinewave.xml"
	@echo "and run the model.."
	@echo "If you look inside, then you get the Sinewave model,"
	@echo "but the icons and locations are screwed up, "
	@echo "because the code generator removes the graphical annotations."
	$(ROOT)/bin/vergil $(ROOT)/ptolemy/actor/lib/test/auto/Sinewave.xml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

