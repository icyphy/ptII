# Makefile for the Java classes used to test the moml package
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2012 The Regents of the University of California.
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

# Location of this directory, relative to the Ptolemy II directory
ME =		ptolemy/moml/test

# Root of the Ptolemy II directory
ROOT =		../../..

# The NoPackageActor.java test needs . in the CLASSPATH.
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR).

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	testDefs.tcl

JSRCS = \
	MoMLParserLeak.java \
	RecorderErrorHandler.java \
	TestIconLoader.java \
	TestLibraryBuilder.java \
	UnloadModelTest.java

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
	_aJVMBug.tcl \
	ConvertToLazy.tcl \
	Documentation.tcl \
	EntityLibrary.tcl \
	ImportAttribute.tcl \
	LevelCrossingLinkTest.tcl \
	LibraryAttribute.tcl \
	LibraryBuilder.tcl \
	Location.tcl \
	MoMLAttribute.tcl \
	MoMLChangeRequest.tcl \
	MoMLCommandLineApplication.tcl \
	MoMLParser.tcl \
	MoMLParserNoPackage.tcl \
	MoMLUndoEntry.tcl \
	MoMLUndoChangeRequest.tcl \
	MoMLVariableChecker.tcl \
	ParserAttribute.tcl \
	parseMoML.tcl \
	StreamErrorHandler.tcl \
	UndoContext.tcl \
	UndoDeleteEntity.tcl \
	UndoDeletePort.tcl \
	UndoDeleteProperty.tcl \
	UndoDeleteRelation.tcl \
	UndoDoc.tcl \
	UndoEntity.tcl \
	UndoLink.tcl \
	UndoPort.tcl \
	UndoProperty.tcl \
	UndoRelation.tcl \
	UndoRename.tcl \
	UndoUnlink.tcl \
	UndoVertex.tcl \
	Vertex.tcl

# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) \
	AltFileNameExceptionTest.xml \
	AltFileNameExceptionTestFile.xml \
	ComplexCompositeCopyAndPasteTest.xml \
	CompositeCopyAndPasteTest.xml \
	CompositeOpaqueCopyAndPasteTest.xml \
	ConvertToLazyTest.xml \
	ConstClassDefinition.xml \
	inputTestB.xml \
	MoMLCommandLineApplicationTest.xml \
	NoPackageActor.java.in \
	NonexistantDirectorTest.xml \
	NoPackageActors.xml \
	UndoDeleteEntityComposite.xml \
	p1.moml \
	p2.moml \
	parseMoMLConst.xml \
        removeconfigure \
	testClass.xml \
	testClassIcon.xml \
	testClass2.xml \
	testClass2DotMoml.moml \
	testdir.moml \
	toplevel.xml \
        validateDemos \
	validateTests \
	 #$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	alljtests.tcl  junit \
	testdir


# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	NoPackageActor.class \
	'UnloadModelTest$$1.class' \
	'UnloadModelTest$$UnloadThread.class'

JCLASS =	$(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple

test_java: jclass

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

USED_CLASSES = $(ROOT)/ptolemy/vergil/toolbox/AnnotationEditorFactory.class

# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(USED_CLASSES) $(KERNEL_TESTDEFS) alljtests.tcl
	$(JTCLSH) alljtests.tcl 

# If "make sources" fails during the build, then 
# AnnotationEditorFactory might not be present
$(ROOT)/ptolemy/vergil/toolbox/AnnotationEditorFactory.class: $(ROOT)/ptolemy/vergil/toolbox/AnnotationEditorFactory.java
	(cd $(ROOT)/ptolemy/vergil/toolbox; $(MAKE))

# Run vergil on each of the demos in compatDemo
demo: compatDemo
	set compatDemo/*.xml; \
	for x do \
		echo $$x;\
		$(ROOT)/bin/vergil $$x; \
	done

# These rules are run from MoMLCommandLineApplication.tcl.  We use
# makefile rules because AUXCLASSPATH may be set to include the
# code coverage harness
MoMLCommandLineApplication1:
	@"$(JAVA)" -classpath "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)" ptolemy.moml.MoMLCommandLineApplication   MoMLCommandLineApplicationTest.xml

MoMLCommandLineApplication2:
	@"$(JAVA)" -classpath "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)" ptolemy.moml.MoMLCommandLineApplication  -myParameter 2 MoMLCommandLineApplicationTest.xml

MoMLCommandLineApplication3:
	@"$(JAVA)" -classpath "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)" ptolemy.moml.MoMLCommandLineApplication  -myParameter "&quot;Hello, World&quot;" MoMLCommandLineApplicationTest.xml

MoMLCommandLineApplication4:
	@"$(JAVA)" -classpath "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(AUXCLASSPATH)" ptolemy.moml.MoMLCommandLineApplication  -SDF\ Director.iterations 2 MoMLCommandLineApplicationTest.xml


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
