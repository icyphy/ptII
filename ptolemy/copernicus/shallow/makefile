# Makefile for Ptolemy II Java shallow code generation
#
# @Authors: Steve Neuendorffer, Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1998-2001 The Regents of the University of California.
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

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(ROOT)/vendors/soot/1.2.2/soot/classes$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

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

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) cg

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	cg

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demo, run 'make demo'"

install: all $(PTPACKAGE).jar

demo: shallowDemo
shallowDemo: compileShallowDemo runOrthoCommDemo timeOrthoCommDemo

TARGETPACKAGE = 	ptolemy.copernicus.shallow.cg
TARGETPATH =		ptolemy/copernicus/shallow/cg

JADTARGETPACKAGE =	ptolemy.copernicus.shallow.cg.jad
JADTARGETPATH =		$(ROOT)/ptolemy/copernicus/shallow/cg/jad

# OrthogonalCom - There is no .xml file for this model
MODEL = OrthogonalCom
SOURCECLASS = ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom

# Usually, we run shallow code gen on OrthogonalCom, but
# if the lines below are uncommented, and we run 
#   make compileShallowDemo runShallowDemo
# then we run on the IIR test model.  To run the demo without editing this
# file, use:
#   make compileShallowDemo runShallowDemo MODEL=IIR SOURCECLASS=../../../ptolemy/actor/lib/test/auto/IIR.xml	
#
#MODEL = IIR
#SOURCECLASS = $(ROOT)/ptolemy/actor/lib/test/auto/$(MODEL).xml


JVM_SIZE = -Xmx256m

# Soot command line arguments for the user.  To list all the arguments, use
# make SOOT_USER_ARGS=-h
SOOT_USER_ARGS =

# Time that we should let code generation run before exiting in ms.
WATCH_DOG_TIMEOUT = 100000

# The tests and the UI use the compileShallowDemo rule, so if you
# change this rule, be sure to run make tests
compileShallowDemo: $(JCLASS)
	@echo "generate shallow code for '$(SOURCECLASS)'"
	$(JAVA) $(JVM_SIZE) \
	 -classpath "$(CLASSPATH)" \
	-Dptolemy.ptII.dir=$(PTII) \
	ptolemy.copernicus.shallow.Main "$(SOURCECLASS)" -d "$(ROOT)"  \
		-p wjtp.watchDog time:$(WATCH_DOG_TIMEOUT) \
		-p wjtp.mt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.writeJimple1 outDir:$(TARGETPATH)/jimple1 \
		-p wjtp.writeJimple2 outDir:$(TARGETPATH)/jimple2 \
		$(SOOT_USER_ARGS)
#	@echo "Here is that javap output"
#	javap -verbose -l -s -classpath $(ROOT) $(SHALLOWTARGETCLASS)

# Partial Java command to run the code generation output.
RUN_COMMAND =	$(JAVA) -Xfuture -classpath "$(ROOT)" \
		-Dptolemy.ptII.dir=$(PTII) \
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
	@echo "Run the model for $(TIMEORTHOCOMM_ITERATIONS) iterations"
	$(RUN_COMMAND) \
		-class $(ORTHOCOMMTARGETCLASS) \
		-director.iterations $(TIMEORTHOCOMM_ITERATIONS) | tail 


# The tests and the UI use the runShallowDemo rule, so if you
# change this rule, be sure to run make tests
SHALLOWTARGETCLASS = $(TARGETPACKAGE).CG$(MODEL)
runShallowDemo:
	@echo "Run the model"
	$(RUN_COMMAND) \
		-class $(SHALLOWTARGETCLASS)

# Jad is a Java disassembler.  Because of licensing restrictions
# jad is not shipped with Ptolemy II
JAD=jad

# Generate a .java file from a class file and recompile it
recompileShallowDemo:
	@echo "Convert .class file back to .java file, compile it, run it"
	$(JAD) -o -d $(JADTARGETPATH) $(ROOT)/$(TARGETPATH)/CG$(MODEL)
	sed 's/$(TARGETPACKAGE)/$(JADTARGETPACKAGE)/' \
		$(JADTARGETPATH)/CG$(MODEL).jad > \
		$(JADTARGETPATH)/CG$(MODEL).jad2
	sh test/jad2java $(JADTARGETPATH)/CG$(MODEL).jad2 \
		> $(JADTARGETPATH)/CG$(MODEL).java
	$(JAVAC) -classpath $(ROOT) $(JADTARGETPATH)/CG$(MODEL).java
	$(JAVA) -Xfuture -classpath "$(ROOT)" \
		ptolemy.actor.gui.CompositeActorApplication \
		-class $(JADTARGETPACKAGE).CG$(MODEL)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

