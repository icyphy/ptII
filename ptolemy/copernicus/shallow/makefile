# Makefile for Ptolemy II Java shallow code generation
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

ME =		ptolemy/copernicus/shallow

# Root of the Java directory
ROOT =		../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	shallow
PTCLASSJAR = 	$(PTPACKAGE).jar

DIRS =		test

# Keep this list alphabetized.
JSRCS = \
	Main.java \
	ShallowModelTransformer.java

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
	cg

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demo, run 'make demo'"

install: all $(PTPACKAGE).jar

demo: compileDemo runDemo

# OrthogonalCom - There is no .xml file for this model
MODEL =			OrthogonalCom
MODELPATH =		$(ROOT)/ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml

# Usually, we run code gen on OrthogonalCom, to run on the IIR model, use:
#   make MODEL=IIR MODELPATH=../../../ptolemy/actor/lib/test/auto/IIR.xml compileDemo runDemo 
# To run on ArrayAppend:
#   make MODEL=ArrayAppend MODELPATH=../../../ptolemy/actor/lib/test/auto/ArrayAppend.xml compileDemo runDemo 

# Location where we create code that we are generating
TARGETPACKAGE = 	ptolemy.copernicus.shallow.cg.$(MODEL)
TARGETPATH =		ptolemy/copernicus/shallow/cg/$(MODEL)

# Package that the decompiler creates classes in
DECOMPILERTARGETPACKAGE =	$(TARGETPACKAGE)
# Directory that the decompiler creates classes in
DECOMPILERTARGETPATH =		$(ROOT)/$(TARGETPATH)

JVMSIZE = 		-Xmx256m

# Soot command line arguments for the user.  To list all the arguments, use
# make COMPILEOPTIONS=-h
COMPILEOPTIONS =

# Time that we should let code generation run before exiting in ms.
WATCHDOGTIMEOUT = 	100000

# The tests and the UI use the compileDemo rule, so if you
# change this rule, be sure to run make tests
compileDemo: $(JCLASS)
	@echo "generate shallow code for '$(MODELPATH)'"
	"$(JAVA)" \
		-classpath "$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)" \
		ptolemy.copernicus.kernel.Copernicus \
		-verbose \
		-codeGenerator   '"shallow"' \
		-compile	 true \
		-iterations      '"$(ITERATIONS)"' \
		-iterationsParameter '"$(ITERATIONSPARAMETER)"' \
		-java            "`echo '"$(JAVA)"' | sed 's@/cygdrive/\([a-z]\)@\1:@'`" \
		-jvmSize         '"$(JVMSIZE)"' \
		-modelPath       '"$(MODELPATH)"' \
		-run		 false \
		-ptIIUserDirectory  "`echo '"$(PTII)"' | sed 's@\\\@/@g'`" \
		-targetPackage   '"$(TARGETPACKAGE)"' \
		-targetPath      '"$(TARGETPATH)"' \
		-watchDogTimeout '"$(WATCHDOGTIMEOUT)"' \
		-compileOptions  '"$(COMPILEOPTIONS)"'

#	@echo "Here is that javap output"
#	javap -verbose -l -s -classpath $(ROOT) $(SHALLOWTARGETCLASS)

# Partial Java command to run the code generation output.
RUN_COMMAND =	"$(JAVA)" -Xfuture -classpath "$(ROOT)" \
		"-Dptolemy.ptII.dir=$(PTII)" \
		ptolemy.actor.gui.CompositeActorApplication

# Run the OrthoComm demo
ORTHOCOMMTARGETCLASS = $(TARGETPACKAGE).CGToplevelModel
runOrthoCommDemo:
	@echo "Run the model"
	$(RUN_COMMAND) \
		-class $(ORTHOCOMMTARGETCLASS) \
		-director.iterations 10

# Time the OrthoComm demo
TIMEORTHOCOMM_ITERATIONS = 50000
timeOrthoCommDemo:
	@echo "Time the model for $(TIMEORTHOCOMM_ITERATIONS) iterations"
	$(RUN_COMMAND) \
		-class $(ORTHOCOMMTARGETCLASS) \
		-director.iterations $(TIMEORTHOCOMM_ITERATIONS) | tail 


# $PTII/util/testsuite/codegen.tcl calls runDemoTest
runDemoTest: runDemo

# The tests and the UI use the runDemo rule, so if you
# change this rule, be sure to run make tests
SHALLOWTARGETCLASS = $(TARGETPACKAGE).CG$(MODEL)
runDemo:
	@echo "Run the model"
	$(RUN_COMMAND) \
		-class $(SHALLOWTARGETCLASS)

# Jad is a Java disassembler.  Because of licensing restrictions
# jad is not shipped with Ptolemy II
JAD=jad

# Generate a .java file from a class file and recompile it
recompileDemo:
	@echo "Convert .class file back to .java file, compile it, run it"
	$(JAD) -o -d $(DECOMPILERTARGETPATH) $(ROOT)/$(TARGETPATH)/CG$(MODEL)
	sed 's/$(TARGETPACKAGE)/$(DECOMPILERTARGETPACKAGE)/' \
		$(DECOMPILERTARGETPATH)/CG$(MODEL).jad > \
		$(DECOMPILERTARGETPATH)/CG$(MODEL).jad2
	sh test/jad2java $(DECOMPILERTARGETPATH)/CG$(MODEL).jad2 \
		> $(DECOMPILERTARGETPATH)/CG$(MODEL).java
	$(JAVAC) -classpath $(ROOT) $(DECOMPILERTARGETPATH)/CG$(MODEL).java
	$(JAVA) -Xfuture -classpath "$(ROOT)" \
		ptolemy.actor.gui.CompositeActorApplication \
		-class $(DECOMPILERTARGETPACKAGE).CG$(MODEL)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

