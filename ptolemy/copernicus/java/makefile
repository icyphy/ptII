# Makefile for Ptolemy II Java deep code generation
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
	ActorTransformer.java \
	CommandLineTemplate.java \
	CommandLineTransformer.java \
	ConstantLoopUnroller.java \
	DeadObjectEliminator.java \
	FieldsForAttributesTransformer.java \
	FieldsForPortsTransformer.java \
	FieldOptimizationTransformer.java \
	InlineDirectorTransformer.java \
	InlineParameterTransformer.java \
	InlinePortTransformer.java \
	InlineTokenTransformer.java \
	Main.java \
	ModelTransformer.java \
	TokenInstanceofEliminator.java \
	TokenToNativeTransformer.java \
	TokenTypeAnalysis.java \
	TypeSpecializer.java \
	TypeTag.java \
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
	cg \
	'CommandLineTemplate$$1.class' \
	'TypeSpecializer$$ConstantTerm.class' \
	'TypeSpecializer$$VariableTerm.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demo, run 'make demo'"

install: all

depend:
	@echo "no dependencies in this directory"

demo: deepDemo
deepDemo: compileDeepDemo runDeepDemo

TARGETPATH =	ptolemy/copernicus/java/cg/shallow

JADTARGETPACKAGE =	ptolemy.copernicus.java.cg.jad
JADTARGETPATH =		$(ROOT)/ptolemy/copernicus/java/cg/jad

# Orthocom
SOURCEPACKAGE = ptolemy.domains.sdf.demo.OrthogonalCom
MODEL = OrthogonalCom 
SOURCECLASS = $(SOURCEPACKAGE).$(MODEL)
#make DEEPSOURCECLASS=../../../ptolemy/actor/lib/test/auto/ArrayAppend.xml MODEL=ArrayAppend compileDeepDemo

TARGETPACKAGE = ptolemy.copernicus.java.cg
TARGETCLASS = $(TARGETPACKAGE).$(MODEL)

JVM_SIZE = -Xmx256m

# Soot command line arguments for the user.  To list all the arguments, use
# make SOOT_USER_ARGS=-h
SOOT_USER_ARGS =

# Time that we should let code generation run before exiting in ms.
WATCH_DOG_TIMEOUT = 300000
compileDeepDemo: $(JCLASS)
	@echo "generate deep code for $(SOURCEPACKAGE)"
	$(JAVA) $(JVM_SIZE) \
	 -classpath "$(CLASSPATH)" \
	ptolemy.copernicus.java.Main $(SOURCECLASS) -d "$(ROOT)"  \
		-p wjtp.watchDog time:$(WATCH_DOG_TIMEOUT) \
		-p wjtp.at targetPackage:$(TARGETPACKAGE) \
		-p wjtp.mt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.clt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.fot targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ffat targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ffpt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.idt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.iat targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ipt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.itt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ttn disabled,targetPackage:$(TARGETPACKAGE) \
		-p wjtp.writeJimple1 outDir:jimple1 \
		-p wjtp.writeJimple2 outDir:jimple2 \
		-p wjtp.iee debug \
		$(SOOT_USER_ARGS)
#	@echo "Here is that javap output"
#	javap -verbose -l -s -classpath $(ROOT) $(TARGETCLASS)

runDeepDemo:
	@echo "Run the deep model"
	$(JAVA) \
	-Xfuture -classpath "$(CLASSPATH)" \
	$(TARGETPACKAGE).Main 

runStaticDeepDemo:
	@echo "Run the deep model static"
	$(JAVA) \
	-Xfuture -classpath "$(CLASSPATH)" \
	$(TARGETPACKAGE).StaticMain 
# -iterations 50

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

