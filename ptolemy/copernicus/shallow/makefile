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
shallowDemo: compileShallowDemo runShallowDemo

TARGETPACKAGE = 	ptolemy.copernicus.shallow.cg
TARGETPATH =		ptolemy/copernicus/shallow/cg

JADTARGETPACKAGE =	ptolemy.copernicus.shallow.cg.jad
JADTARGETPATH =		$(ROOT)/ptolemy/copernicus/shallow/cg/jad

# IIR
MODEL = IIR
SOURCECLASS = $(ROOT)/ptolemy/actor/lib/test/auto/$(MODEL).xml
SHALLOWTARGETCLASS = $(TARGETPACKAGE).CG$(MODEL)

JVM_SIZE = -Xmx256m

# make SOURCECLASS=../test/RampFileWriter.xml compileDemo

# Soot command line arguments for the user.  To list all the arguments, use
# make SOOT_USER_ARGS=-h
SOOT_USER_ARGS =

# Time that we should let code generation run before exiting in ms.
WATCH_DOG_TIMEOUT = 100000
compileShallowDemo: $(JCLASS)
	@echo "generate shallow code for '$(SOURCECLASS)'"
	$(JAVA) $(JVM_SIZE) \
	 -classpath "$(CLASSPATH)" \
	-Dptolemy.ptII.dir=$(PTII) \
	ptolemy.copernicus.shallow.Main "$(SOURCECLASS)" -d "$(ROOT)"  \
		-p wjtp.watchDog time:$(WATCH_DOG_TIMEOUT) \
		-p wjtp.mt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.writeJimple1 outDir:$(TARGETPACKAGE)/jimple1 \
		-p wjtp.writeJimple2 outDir:$(TARGETPACKAGE)/jimple2 \
		$(SOOT_USER_ARGS)
#	@echo "Here is that javap output"
#	javap -verbose -l -s -classpath $(ROOT) $(SHALLOWTARGETCLASS)

runShallowDemo:
	@echo "Run the model"
	$(JAVA) -Xfuture -classpath "$(ROOT)" \
		-Dptolemy.ptII.dir=$(PTII) \
		ptolemy.actor.gui.CompositeActorApplication \
		-class $(SHALLOWTARGETCLASS)

# Jad is a Java disassembler
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

