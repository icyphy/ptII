# Makefile for Ptolemy II Java interpreted execution (e.g., no code gen at all)
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

ME =		ptolemy/copernicus/interpreted

DIRS =

# Root of the Java directory
ROOT =		../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	interpreted
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	InterpretedWriter.java \
	Main.java

OTHER_FILES_TO_BE_JARED = \
	compileCommandTemplate.txt \
	makefile.in \
	runCommandTemplate.txt

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	cg \

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demo, run 'make demo'"

install: jclass $(PTCLASSJAR)

demo: compileDemo runDemo

# OrthogonalCom
MODEL =			OrthogonalCom
MODELPATH =		$(ROOT)/ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml

# Location where we create code that we are generating
TARGETPACKAGE = 	ptolemy.copernicus.interpreted.cg.$(MODEL)
TARGETPATH =		ptolemy/copernicus/interpreted/cg/$(MODEL)

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

compileDemo: $(JCLASS)
	@echo "generate interpreted code for $(MODELPATH)"
	$(PTII)/bin/copernicus \
		-verbose \
		-codeGenerator   "interpreted" \
		-compile	 true \
		-iterations      "$(ITERATIONS)" \
		-iterationsParameter "$(ITERATIONSPARAMETER)" \
		-java            "`echo "$(JAVA)" | sed 's@/cygdrive/\([a-z]\)@\1:@'`" \
		-jvmSize         "$(JVMSIZE)" \
		-modelPath       "$(MODELPATH)" \
		-run		 false \
		-ptIIUserDirectory  "`echo "$(PTII)" | sed 's@\\\@/@g'`" \
		-targetPackage   "$(TARGETPACKAGE)" \
		-targetPath      "$(TARGETPATH)" \
		-watchDogTimeout "$(WATCHDOGTIMEOUT)"

#       -compileOptions  "$(COMPILEOPTIONS)"


#	@echo "Here is that javap output"
#	javap -verbose -l -s -classpath $(ROOT) $(TARGETCLASS)

# Use MoMLSimpleApplication here so that the demo can run without a head.
runDemo:
	@echo "Run the interpreted model"
	"$(JAVA)" \
		-Xfuture -classpath "$(CLASSPATH)" \
		ptolemy.actor.gui.MoMLSimpleApplication \
		$(MODELPATH)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

