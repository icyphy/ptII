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

ME =		ptolemy/copernicus/java

DIRS =		test

# Root of the Java directory
ROOT =		../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	java
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	AtomicActorCreator.java \
	CodeGenerationScope.java \
	CommandLineTemplate.java \
	CommandLineTransformer.java \
	ConstantLoopUnroller.java \
	ConstructorSpecializer.java \
	DataUtilities.java \
	DeadObjectEliminator.java \
	ExceptionEliminator.java \
	ExpressionCreator.java \
	FieldsForAttributesTransformer.java \
	FieldsForEntitiesTransformer.java \
	FieldsForPortsTransformer.java \
	FieldOptimizationTransformer.java \
	FinalFieldUnfinalizer.java \
	FSMCreator.java \
	GenericAtomicActorCreator.java \
	GiottoPortInliner.java \
	HSPortInliner.java \
	InlineDirectorTransformer.java \
	InlineParameterTransformer.java \
	InlinePortTransformer.java \
	InlineTokenTransformer.java \
	Main.java \
	ModelTransformer.java \
	NamedObjAnalysis.java \
	NamedObjEliminator.java \
	NamedObjEqualityEliminator.java \
	ParseTreeCodeGenerator.java \
	PortInliner.java \
	ReplaceComplexParameters.java \
	SDFPortInliner.java \
	TestApplication.java \
	TokenConstructorAnalysis.java \
	TokenInstanceofEliminator.java \
	TokenToNativeTransformer.java \
	TokenTypeAnalysis.java \
	TypeSpecializer.java \
	TypeSpecializerAnalysis.java \
	TypeTag.java \
	UnreachableMethodRemover.java \
	ValueTag.java

OTHER_FILES_TO_BE_JARED = \
	makefile.in \
	obfuscateScript.jos.in \
	runCommandTemplate.txt

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) \
	Orthocom.jos

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	cg \
	'CommandLineTemplate$$1.class' \
	'TypeSpecializer$$ConstantTerm.class' \
	'TypeSpecializer$$VariableTerm.class' \
	'TypeSpecializerAnalysis$$ConstantTerm.class' \
	'TypeSpecializerAnalysis$$VariableTerm.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demo, run 'make demo'"

install: jclass $(PTCLASSJAR)

demo: compileDemo runDemo
timeDemo: compileDemo timeOrthoCommDemo

# OrthogonalCom
MODEL =			OrthogonalCom
MODELPATH =		$(ROOT)/ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml

# Usually, we run code gen on OrthogonalCom, to run on the IIR model, use:
#  make MODELPATH=../../../ptolemy/actor/lib/test/auto/CommDistDiscard.xml MODEL=CommDistDiscard compileDemo runDemo 
# To run on ArrayAppend:
#  make MODELPATH=../../../ptolemy/actor/lib/test/auto/ArrayAppend.xml MODEL=ArrayAppend compileDemo runDemo

# Location where we create code that we are generating
TARGETPACKAGE = 	ptolemy.copernicus.java.cg.$(MODEL)
TARGETPATH =		ptolemy/copernicus/java/cg/$(MODEL)

TARGETCLASS = $(TARGETPACKAGE).$(MODEL)

# Package that the decompiler creates classes in
DECOMPILERTARGETPACKAGE =	$(TARGETPACKAGE)
# Directory that the decompiler creates classes in
DECOMPILERTARGETPATH =		$(ROOT)/$(TARGETPATH)

JVMSIZE = -Xmx256m

# Soot command line arguments for the user.  To list all the arguments, use
# make COMPILEOPTIONS=-h
#COMPILEOPTIONS= -compileOptions -h

# Time that we should let code generation run before exiting in ms.
WATCHDOGTIMEOUT =     600000
# Number of Iterations to run the generated code for.
ITERATIONS =		1000
ITERATIONSPARAMETER =	,iterations:$(ITERATIONS)

compileDemo: $(JCLASS)
	@echo "generate deep code for $(MODELPATH)"
	$(PTII)/bin/copernicus \
		-verbose \
		-compile	 true \
		-iterations      "$(ITERATIONS)" \
		-iterationsParameter "$(ITERATIONSPARAMETER)" \
		-java            "`echo "$(JAVA)" | sed 's@/cygdrive/\([a-z]\)@\1:@'`" \
		-jvmSize         "$(JVMSIZE)" \
		-run		 false \
		-ptIIUserDirectory  "`echo "$(PTII)" | sed 's@\\\@/@g'`" \
		-targetPackage   "$(TARGETPACKAGE)" \
		-targetPath      "$(TARGETPATH)" \
		-watchDogTimeout "$(WATCHDOGTIMEOUT)" \
		$(MODELPATH)

#	@echo "Here is that javap output"
#	javap -verbose -l -s -classpath $(ROOT) $(TARGETCLASS)

runDemo:
	(cd cg/$(MODEL); $(MAKE) longTest)

treeShakeDemo:
	cd $(ROOT)/$(TARGETPATH); $(MAKE) treeShake

# $PTII/util/testsuite/codegen.tcl calls runDemoTest
runDemoTest: treeShakeAll

# Treeshake everything and run it
treeShakeAll: treeShakeWithoutCodegen runTreeShakeWithoutCodegen \
		treeShakeAndRunSnapshots \
		treeShakeCodegen runTreeShakeCodegen \
		compareTreeShakeJars

treeShakeWithoutCodegen:
	cd $(ROOT)/$(TARGETPATH); $(MAKE) $@

runTreeShakeWithoutCodegen:
	cd $(ROOT)/$(TARGETPATH); $(MAKE) $@

treeShakeAndRunSnapshots:
	cd $(ROOT)/$(TARGETPATH); $(MAKE) $@

treeShakeCodegen:
	cd $(ROOT)/$(TARGETPATH); $(MAKE) $@

runTreeShakeCodegen:
	cd $(ROOT)/$(TARGETPATH); $(MAKE) $@

compareTreeShakeJars:
	cd $(ROOT)/$(TARGETPATH); $(MAKE) $@


# Generate a .java file from a class file and recompile it
jodeDecompile: jodeDecompileDemo jodeRecompileDemo jodeRunRecompiledDemo
jodeDecompileDemo:
	cd $(ROOT)/$(TARGETPATH); $(MAKE) $@
jodeRecompileDemo:
	cd $(ROOT)/$(TARGETPATH); $(MAKE) $@
jodeRunRecompiledDemo:
	cd $(ROOT)/$(TARGETPATH); $(MAKE) $@
jadDecompile:
	cd $(ROOT)/$(TARGETPATH); $(MAKE) $@

# Generate .java files from the snapshots
recompileSnapshots:
	cd $(ROOT)/$(TARGETPATH); $(MAKE) $@

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

