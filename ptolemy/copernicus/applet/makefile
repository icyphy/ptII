# Makefile for Ptolemy II applet generation
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 2001-2003 The Regents of the University of California.
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

ME =		ptolemy/copernicus/applet

# Root of the Java directory
ROOT =		../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	applet
PTCLASSJAR = 	$(PTPACKAGE).jar

DIRS =		test

# Keep this list alphabetized.
JSRCS = \
	AppletWriter.java \
	Main.java

OTHER_FILES_TO_BE_JARED = \
	compileCommandTemplate.txt \
	default.css \
	makefile.in \
	model.htm.in \
	modelVergil.htm.in \
	runCommandTemplate.txt

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	cg \
	doc

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demo, run 'make demo'"

install: all $(PTPACKAGE).jar

demo: compileDemo runDemo

# OrthogonalCom
MODEL =			OrthogonalCom
MODELPATH =		$(ROOT)/ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml

# 
#MODEL =			modulation
#MODELPATH = 		../../../ptolemy/moml/demo/modulation.xml

MODEL =			Butterfly
MODELPATH = 		$(ROOT)/ptolemy/domains/sdf/demo/Butterfly/Butterfly.xml

# Usually, we run code gen on OrthogonalCom, to run on the IIR model, use:
#   make MODEL=IIR MODELPATH=../../../ptolemy/actor/lib/test/auto/IIR.xml compileDemo runDemo 
# To run on ArrayAppend:
#   make MODEL=ArrayAppend MODELPATH=../../../ptolemy/actor/lib/test/auto/ArrayAppend.xml compileDemo runDemo 

# Location where we create code that we are generating
TARGETPACKAGE = 	ptolemy.copernicus.applet.cg.$(MODEL)
TARGETPATH =		ptolemy/copernicus/applet/cg/$(MODEL)

# Package that the decompiler creates classes in
DECOMPILERTARGETPACKAGE =	$(TARGETPACKAGE)
# Directory that the decompiler creates classes in
DECOMPILERTARGETPATH =		$(ROOT)/$(TARGETPATH)

JVM_SIZE = 		-Xmx256m

# Soot command line arguments for the user.  To list all the arguments, use
# make SOOT_USER_ARGS=-h
SOOT_USER_ARGS =

# Time that we should let code generation run before exiting in ms.
WATCH_DOG_TIMEOUT = 	100000

# The tests and the UI use the compileDemo rule, so if you
# change this rule, be sure to run make tests
compileDemo: $(JCLASS)
	@echo "generate an applet for '$(MODELPATH)'"
	"$(JAVA)" \
		-classpath "$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)" \
		ptolemy.copernicus.kernel.Copernicus \
		-verbose \
		-codeGenerator   '"applet"' \
		-compile	 true \
		-java            "`echo '"$(JAVA)"' | sed 's@/cygdrive/\([a-z]\)@\1:@'`" \
		-modelPath       '"$(MODELPATH)"' \
		-run		 false \
		-ptIIUserDirectory  "`echo '"$(PTII)"' | sed 's@\\\@/@g'`" \
		-targetPackage   '"$(TARGETPACKAGE)"' \
		-targetPath      '"$(TARGETPATH)"'


# Run the OrthoComm demo
ORTHOCOMMTARGETDIRECTORY = $(TARGETPATH)/ToplevelModel
runOrthoCommDemo:
	@echo "Run the OrthoComm model"
	cd $(ROOT)/$(ORTHOCOMMTARGETDIRECTORY); appletviewer ToplevelModel.htm
	cd $(ROOT)/$(ORTHOCOMMTARGETDIRECTORY); appletviewer ToplevelModelVergil.htm

# Time the OrthoComm demo
TIMEORTHOCOMM_ITERATIONS = 50000
timeOrthoCommDemo:
	@echo "No need to time anything in the applet directory"

APPLETTARGETDIRECTORY = $(TARGETPATH)
runDemo:
	@echo "Run the model"
	cd $(ROOT)/$(APPLETTARGETDIRECTORY); appletviewer $(MODEL).htm
	cd $(ROOT)/$(APPLETTARGETDIRECTORY); appletviewer $(MODEL)Vergil.htm

# Test writing in a separate directory
test1: $(JCLASS)
	@echo "generate an applet for '$(MODELPATH)'"
	"$(JAVA)" \
		-classpath "$(ROOT)" \
		ptolemy.copernicus.kernel.Copernicus \
		-verbose \
		-codeGenerator   '"applet"' \
		-compile	 true \
		-java            "`echo '"$(JAVA)"' | sed 's@/cygdrive/\([a-z]\)@\1:@'`" \
		-modelPath       '"$(MODELPATH)"' \
		-run		 true \
		-ptIIUserDirectory   '"d:/tmp/test directory"' \
		-targetPackage   '"$(MODEL)"'



# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

