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
	TokenConstructorAnalysis.java \
	TokenInstanceofEliminator.java \
	TokenToNativeTransformer.java \
	TokenTypeAnalysis.java \
	TypeSpecializer.java \
	TypeTag.java \
	UnreachableMethodRemover.java \
	ValueTag.java	

#	NamedObjConstructorAnalysis.java

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

demo: compileDemo runDemo
timeDemo: compileDemo timeOrthoCommDemo

# OrthogonalCom - There is no .xml file for this model
MODEL =			OrthogonalCom
SOURCECLASS =		ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom

# Usually, we run code gen on OrthogonalCom, to run on the IIR model, use:
#  make SOURCECLASS=../../../ptolemy/actor/lib/test/auto/CommDistDiscard.xml MODEL=CommDistDiscard compileDemo runDemo 
# To run on ArrayAppend:
#  make SOURCECLASS=../../../ptolemy/actor/lib/test/auto/ArrayAppend.xml MODEL=ArrayAppend compileDemo runDemo

# Location where we create code that we are generating
TARGETPACKAGE = 	ptolemy.copernicus.java.cg.$(MODEL)
TARGETPATH =		ptolemy/copernicus/java/cg/$(MODEL)

TARGETCLASS = $(TARGETPACKAGE).$(MODEL)

# Package that the decompiler creates classes in
DECOMPILERTARGETPACKAGE =	$(TARGETPACKAGE)
# Directory that the decompiler creates classes in
DECOMPILERTARGETPATH =		$(ROOT)/$(TARGETPATH)

JVM_SIZE = -Xmx256m

# Soot command line arguments for the user.  To list all the arguments, use
# make SOOT_USER_ARGS=-h
SOOT_USER_ARGS =

# Time that we should let code generation run before exiting in ms.
WATCH_DOG_TIMEOUT =     600000
# Number of Iterations to run the generated code for.
ITERATIONS =		200
ITERATIONS_PARAMETER =	,iterations:$(ITERATIONS)
compileDemo: $(JCLASS)
	@echo "generate deep code for $(SOURCECLASS)"
	$(JAVA) $(JVM_SIZE) \
	 -classpath "$(CLASSPATH)" \
	 -Dptolemy.ptII.dir=$(PTII) \
	ptolemy.copernicus.java.Main $(SOURCECLASS) -d "$(ROOT)"  \
		-p wjtp.watchDog time:$(WATCH_DOG_TIMEOUT) \
		-p wjtp.at targetPackage:$(TARGETPACKAGE) \
		-p wjtp.mt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.clt \
		targetPackage:$(TARGETPACKAGE)$(ITERATIONS_PARAMETER) \
		-p wjtp.fot targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ffat targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ffpt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.idt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.iat targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ipt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.itt targetPackage:$(TARGETPACKAGE) \
		-p wjtp.ttn disabled,targetPackage:$(TARGETPACKAGE) \
		-p wjtp.snapshot1 outDir:$(ROOT)/$(TARGETPATH)/jimple1 \
		-p wjtp.snapshot2 outDir:$(ROOT)/$(TARGETPATH)/jimple2 \
		-p wjtp.snapshot3 outDir:$(ROOT)/$(TARGETPATH)/jimple3 \
		-p wjtp.snapshot4 outDir:$(ROOT)/$(TARGETPATH)/jimple4 \
		-p wjtp.snapshot5 outDir:$(ROOT)/$(TARGETPATH)/jimple5 \
		-p wjtp.snapshot6 outDir:$(ROOT)/$(TARGETPATH)/jimple6 \
		-p wjtp.iee enabled \
		$(SOOT_USER_ARGS)
#	@echo "Here is that javap output"
#	javap -verbose -l -s -classpath $(ROOT) $(TARGETCLASS)

# Directories that contain snapshots
SNAPSHOTS=jimple1 jimple2 jimple3 jimple4 jimple5

# Run the simulation in each snapshot directory
runSnapshots:
	@echo "Run without codegen"
	CLASSPATH="$(CLASSPATH)" \
		$(JAVA) ptolemy.actor.gui.CompositeActorApplication \
		-class $(SOURCECLASS) \
		-iterations $(ITERATIONS) | tail -1
	@echo "Run the snapshots"
	for snapshot in $(SNAPSHOTS); \
	do \
		echo "Running the code from $(TARGETPATH)/$$snapshot"; \
		$(JAVA) \
			-Xfuture -classpath \
	"$(ROOT)/$(TARGETPATH)/$$snapshot$(CLASSPATHSEPARATOR)$(CLASSPATH)" \
			$(TARGETPACKAGE).Main | tail -1 ; \
	done
	@echo "Run the final version"
	$(JAVA) \
		-Xfuture -classpath "$(CLASSPATH)" \
		$(TARGETPACKAGE).Main | tail -1 

runDemo:
	@echo "Run the deep model"
	$(JAVA) \
	-Xfuture -classpath "$(CLASSPATH)" \
	$(TARGETPACKAGE).Main 

timeOrthoCommDemo:
	@echo "Time the model for $(ITERATIONS) iterations"
	$(JAVA) \
	-Xfuture -classpath "$(CLASSPATH)" \
	$(TARGETPACKAGE).Main  | tail

runStaticDemo:
	@echo "Run the deep model static"
	$(JAVA) \
	-Xfuture -classpath "$(CLASSPATH)" \
	$(TARGETPACKAGE).StaticMain 
# -iterations 50


# Class to disassemble
JADTARGETCLASS =		Main

# Generate a .java file from a class file and recompile it
recompileDemo:
	@echo "Convert .class file back to .java file, compile it, run it"
	$(JAD) -o -d $(JADTARGETPATH) $(JADTARGET)/$(JADTARGETCLASS)
	sed 's/$(TARGETPACKAGE)/$(JADTARGETPACKAGE)/' \
		$(JADTARGETPATH)/$(JADTARGETCLASS).jad > \
		$(JADTARGETPATH)/$(JADTARGETCLASS).jad2
	sh ../shallow/test/jad2java $(JADTARGETPATH)/$(JADTARGETCLASS).jad2 \
		> $(JADTARGETPATH)/$(JADTARGETCLASS).java
	$(JAVAC) -classpath $(ROOT) $(JADTARGETPATH)/$(JADTARGETCLASS).java
	$(JAVA) -Xfuture -classpath "$(ROOT)" \
		ptolemy.actor.gui.CompositeActorApplication \
		-class $(JADTARGETPACKAGE).$(JADTARGETCLASS)


# Generate .java files from the snapshots
recompileSnapshots:
	for snapshot in $(SNAPSHOTS); \
	do \
		echo "Recompiling in $(TARGETPATH)/$$snapshot"; \
		$(JAD) -r -o -d $(ROOT)/$(TARGETPATH)/$$snapshot/jad $(ROOT)/$(TARGETPATH)/$$snapshot/$(TARGETPATH)/*.class; \
		for file in $(ROOT)/$(TARGETPATH)/$$snapshot/jad/$(TARGETPATH)/*.jad; \
		do \
			echo "Fixing $$file"; \
			javafile=`dirname $$file`/`basename $$file .jad`.java;\
			sh ../shallow/test/jad2java $$file \
				> $$javafile; \
			echo "Compilinmg $$javafile"; \
			$(JAVAC) -classpath "$(ROOT)/$(TARGETPATH)/$$snapshot/jad$(CLASSPATHSEPARATOR)$(ROOT)" \
				$$javafile; \
		done; \
	done


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

