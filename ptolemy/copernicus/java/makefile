# Makefile for Ptolemy II Java deep and shallow code generation
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

ME =		ptolemy/copernicus/java

# Root of the Java directory
ROOT =		../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

DIRS =		test

# Keep this list alphabetized.
JSRCS = \
	CommandLineTransformer.java \
	EntitySootClass.java \
	InlineParameterTransformer.java \
	Main.java \
	ModelTransformer.java

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
	@echo "To run the demo, run 'make demo' or 'make shallowDemo' or 'make deepDemo'"

install: all

depend:
	@echo "no dependencies in this directory"

demo: shallowDemo deepDemo
shallowDemo: compileShallowDemo runShallowDemo
deepDemo: compileDeepDemo runDeepDemo

SOURCEPACKAGE = ptolemy.domains.sdf.demo.OrthogonalCom
MODEL = OrthogonalCom 
SOURCECLASS = $(SOURCEPACKAGE).$(MODEL)

DEEPTARGETPACKAGE = ptolemy.copernicus.java.cg.deep
SHALLOWTARGETPACKAGE = ptolemy.copernicus.java.cg.shallow
DEEPTARGETCLASS = $(DEEPTARGETPACKAGE).$(MODEL)
SHALLOWTARGETCLASS = $(SHALLOWTARGETPACKAGE).$(MODEL)

JVM_SIZE = -Xmx132m

# make SOURCECLASS=../test/RampFileWriter.xml compileDemo

# Soot command line arguments for the user.  To list all the arguments, use
# make SOOT_USER_ARGS=-h
SOOT_USER_ARGS = 

compileShallowDemo: $(JCLASS)
	@echo "generate shallow code for $(SOURCEPACKAGE)"
	$(JAVA) $(JVM_SIZE) \
	 -classpath "$(CLASSPATH)" \
	ptolemy.copernicus.java.Main $(SOURCECLASS) -d "$(ROOT)"  \
		-p wjtp.at targetPackage:$(SHALLOWTARGETPACKAGE) \
		-p wjtp.mt targetPackage:$(SHALLOWTARGETPACKAGE) \
		$(SOOT_USER_ARGS)
	@echo "Here is that javap output"
	javap -verbose -l -s -classpath $(ROOT) $(SHALLOWTARGETCLASS)

runShallowDemo:
	@echo "Run the model"
	$(JAVA) \
	-Xfuture -classpath "$(CLASSPATH)" \
	ptolemy.actor.gui.CompositeActorApplication \
	-iterations 50 \
	-class $(SHALLOWTARGETCLASS)

compileDeepDemo: $(JCLASS)
	@echo "generate deep code for $(SOURCEPACKAGE)"
	$(JAVA) $(JVM_SIZE) \
	 -classpath "$(CLASSPATH)" \
	ptolemy.copernicus.java.Main $(SOURCECLASS) -d "$(ROOT)"  \
		-p wjtp.at deep,targetPackage:$(DEEPTARGETPACKAGE) \
		-p wjtp.mt deep,targetPackage:$(DEEPTARGETPACKAGE) \
		-p wjtp.clt deep,targetPackage:$(DEEPTARGETPACKAGE) \
		$(SOOT_USER_ARGS)
	@echo "Here is that javap output"
	javap -verbose -l -s -classpath $(ROOT) $(DEEPTARGETCLASS)

runDeepDemo:
	@echo "Run the deep model"
	$(JAVA) \
	-Xfuture -classpath "$(CLASSPATH)" \
	$(DEEPTARGETPACKAGE).Main 
# -iterations 50

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

