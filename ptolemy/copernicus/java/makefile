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

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(ROOT)/vendors/soot/1.2.2/soot/classes$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	java
PTCLASSJAR = 	$(PTPACKAGE).jar

DIRS =		test

# Keep this list alphabetized.
JSRCS = \
	CommandLineTemplate.java \
	CommandLineTransformer.java \
	ConstantLoopUnroller.java \
	DeadObjectEliminator.java \
	EntitySootClass.java \
	FieldsForAttributesTransformer.java \
	FieldsForPortsTransformer.java \
	FieldOptimizationTransformer.java \
	InlineDirectorTransformer.java \
	InlineParameterTransformer.java \
	InlinePortTransformer.java \
	InlineTokenTransformer.java \
	Main.java \
	ModelTransformer.java \
	PtolemyUtilities.java \
	TokenInstanceofEliminator.java \
	TokenToNativeTransformer.java \
	TokenTypeAnalysis.java \
	TypeSpecializer.java \
	UnreachableMethodRemover.java \
	ValueTag.java	

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

SHALLOWTARGETPACKAGE = ptolemy.copernicus.java.cg.shallow

# Orthocom
DEEPSOURCEPACKAGE = ptolemy.domains.sdf.demo.OrthogonalCom
DEEPMODEL = OrthogonalCom 
DEEPSOURCECLASS = $(SOURCEPACKAGE).$(MODEL)
DEEPTARGETCLASS = $(SHALLOWTARGETPACKAGE).CGToplevelModel

# IIR
MODEL = IIR
SOURCECLASS = $(ROOT)/ptolemy/actor/lib/test/auto/$(MODEL).xml
SHALLOWTARGETCLASS = $(SHALLOWTARGETPACKAGE).CG$(MODEL)


DEEPTARGETPACKAGE = ptolemy.copernicus.java.cg.deep
DEEPTARGETCLASS = $(DEEPTARGETPACKAGE).$(MODEL)


JVM_SIZE = -Xmx256m

# make SOURCECLASS=../test/RampFileWriter.xml compileDemo

# Soot command line arguments for the user.  To list all the arguments, use
# make SOOT_USER_ARGS=-h
SOOT_USER_ARGS =

compileShallowDemo: $(JCLASS)
	@echo "generate shallow code for $(SOURCECLASS)"
	$(JAVA) $(JVM_SIZE) \
	 -classpath "$(CLASSPATH)" \
	ptolemy.copernicus.java.Main $(SOURCECLASS) -d "$(ROOT)"  \
		-p wjtp.at targetPackage:$(SHALLOWTARGETPACKAGE) \
		-p wjtp.mt targetPackage:$(SHALLOWTARGETPACKAGE) \
		-p wjtp.umr disabled \
		$(SOOT_USER_ARGS)
#	@echo "Here is that javap output"
#	javap -verbose -l -s -classpath $(ROOT) $(SHALLOWTARGETCLASS)

runShallowDemo:
	@echo "Run the model"
	$(JAVA) -Xfuture -classpath "$(ROOT)" \
		ptolemy.actor.gui.CompositeActorApplication \
		-class $(SHALLOWTARGETCLASS)

compileDeepDemo: $(JCLASS)
	@echo "generate deep code for $(DEEPSOURCEPACKAGE)"
	$(JAVA) $(JVM_SIZE) \
	 -classpath "$(CLASSPATH)" \
	ptolemy.copernicus.java.Main $(DEEPSOURCECLASS) -d "$(ROOT)"  \
		-p wjtp.at deep,targetPackage:$(DEEPTARGETPACKAGE) \
		-p wjtp.mt deep,targetPackage:$(DEEPTARGETPACKAGE) \
		-p wjtp.clt deep,targetPackage:$(DEEPTARGETPACKAGE) \
		-p wjtp.fot deep,targetPackage:$(DEEPTARGETPACKAGE) \
		-p wjtp.ffat deep,targetPackage:$(DEEPTARGETPACKAGE) \
		-p wjtp.ffpt deep,targetPackage:$(DEEPTARGETPACKAGE) \
		-p wjtp.idt deep,targetPackage:$(DEEPTARGETPACKAGE) \
		-p wjtp.iat deep,targetPackage:$(DEEPTARGETPACKAGE) \
		-p wjtp.ipt deep,targetPackage:$(DEEPTARGETPACKAGE) \
		-p wjtp.itt deep,targetPackage:$(DEEPTARGETPACKAGE) \
		-p wjtp.ttn deep,disabled,targetPackage:$(DEEPTARGETPACKAGE) \
		-p wjtp.writeJimple1 outDir:jimple1 \
		-p wjtp.writeJimple2 outDir:jimple2 \
		-p wjtp.ts deep \
		-p wjtp.umr deep \
		$(SOOT_USER_ARGS)
#	@echo "Here is that javap output"
#	javap -verbose -l -s -classpath $(ROOT) $(DEEPTARGETCLASS)

runDeepDemo:
	@echo "Run the deep model"
	$(JAVA) \
	-Xfuture -classpath "$(CLASSPATH)" \
	$(DEEPTARGETPACKAGE).Main 

runStaticDeepDemo:
	@echo "Run the deep model static"
	$(JAVA) \
	-Xfuture -classpath "$(CLASSPATH)" \
	$(DEEPTARGETPACKAGE).StaticMain 
# -iterations 50

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

