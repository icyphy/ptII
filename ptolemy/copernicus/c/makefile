# Makefile for Ptolemy II C code generation back-end
#
# @Authors:  Christopher Hylands, Ankush Varma, Shuvra S. Bhattacharyya --
# based on a file by Thomas M. Parks
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

ME =		ptolemy/copernicus/c

DIRS =		test runtime

# Root of the Java directory
ROOT =		../../..

# $PTII/configure looks for Soot and JHDL and sets
# variables in $PTII/mk/ptII.mk
CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	c
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	runtime/runtime.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
        AnalysisUtilities.java \
        CallGraphPruner.java \
        ClassStructureGenerator.java \
	CNames.java \
	CodeFileGenerator.java \
	CodeGenerator.java \
	Context.java \
	CSwitch.java \
	CWriter.java \
	ExceptionTracker.java \
	FileHandler.java \
	HeaderFileGenerator.java \
        InstanceStructureGenerator.java \
	JavaToC.java \
	Main.java \
        MainFileGenerator.java \
	MakeFileGenerator.java \
	MethodCodeGenerator.java \
	MethodListGenerator.java \
	NativeMethodGenerator.java \
        Options.java \
	OverriddenMethodGenerator.java \
	RequiredFileGenerator.java \
	StubFileGenerator.java \
	Utilities.java

# Omitted Files

OTHER_FILES_TO_BE_JARED = \
	runCommandTemplate.txt

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) \
	README.txt


# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To run the demo, cd to 'test', and run 'make demo'."

install: all jars

depend:
	@echo "no dependencies in this directory"

# OrthogonalCom
MODEL =			OrthogonalCom
MODELPATH =		$(ROOT)/ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.xml

# Usually, we run code gen on OrthogonalCom, to run on the IIR model, use:
#  make MODELPATH=../../../ptolemy/actor/lib/test/auto/CommDistDiscard.xml MODEL=CommDistDiscard compileDemo runDemo 
# To run on ArrayAppend:
#  make MODELPATH=../../../ptolemy/actor/lib/test/auto/ArrayAppend.xml MODEL=ArrayAppend compileDemo runDemo

OUTPUT_DIR =	cg/$(MODEL)

#demo: compileDemo runDemo
demo:
	cd test; make demo; cd ..

compileDemo: $(JCLASS)
	@echo "generate c code for $(MODELPATH)"
	rm -rf $(OUTPUT_DIR)
	mkdir -p $(OUTPUT_DIR)
	"$(JAVA)" \
	-classpath "$(CLASSPATH)" \
	ptolemy.copernicus.c.Main \
		$(MODELPATH) \
		-d ../../nonexistent/something \
		--debug \
		-p wjtp.snapshot1 \
		outDir:$(OUTPUT_DIR)

runDemo:
	cd cg/$(MODEL)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

