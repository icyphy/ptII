# Ptolemy II Java Code generation lexical analyzer test directory
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2000-2001 The Regents of the University of California.
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

ME =		ptolemy/lang/java/test

DIRS =

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)


# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	testDefs.tcl

JSRCS = \
	Anon.java \
	ArrayLength.java \
	CharTest.java \
	Child.java \
	ClassAccess.java \
	ExceptionTest.java \
	Fields.java \
	FindExtraImportsVisitorTest.java \
	For.java \
	IFace.java \
	InnerClass.java \
	InnerIFace.java \
	Methods.java \
	OneFM.java \
	OneField.java \
	ReflectTest.java \
	ReflectTestFields.java \
	ReflectTestInterface.java \
	Simple.java \
	Simple2.java \
	Switch.java \
	Super.java \
	SuperChild.java \
	UseFields.java \
	UseMethods.java

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
	ASTReflect.tcl \
	FindExtraImportsVisitor.tcl \
	JavaCodeGenerator.tcl \
	SearchPath.tcl \
	Yylex.tcl


# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) \
	JavaScope.java \
	$(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	alljtests.tcl

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'Anon$$1.class' \
	'InnerClass$$InnerInnerClass.class' \
	'InnerIFace$$IFace.class' \
	'ReflectTest$$innerPublicClass.class' \
	$(PTCLASSJAR)

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_jsimple \
		ASTReflect PrintTree RegenerateCode codegen

# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl
	$(JTCLSH) alljtests.tcl

# Print out the AST for a class
ASTReflect:
	CLASSPATH=$(CLASSPATH) $(JAVA) \
		ptolemy.lang.java.ASTReflect ptolemy.lang.java.nodetypes.IntLitNode

PrintTree:
	CLASSPATH=$(CLASSPATH) $(JAVA) \
		ptolemy.lang.java.PrintTree ../nodetypes/IntLitNode.java

RegenerateCode:
	CLASSPATH=$(CLASSPATH) $(JAVA) \
		ptolemy.lang.java.RegenerateCode ../nodetypes/IntLitNode.java


codegen: $(ROOT)/ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.class \
		generate_codegen compile_codegen run_codegen

$(ROOT)/ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.class: \
	$(ROOT)/ptolemy/domains/sdf/demo/OrthogonalCom/OrthogonalCom.java
	(cd $(ROOT)/ptolemy/domains/sdf/demo/OrthogonalCom; $(MAKE))

generate_codegen:
	CLASSPATH="$(CLASSPATH)" \
	$(JAVA) -DPTII=$(ROOT) \
		ptolemy.domains.sdf.codegen.SDFCodeGenerator \
		-class  ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom \
		-iterations 500 \
		-outdir $(ROOT) -outpkg cg.OrthogonalCom
compile_codegen:
	(cd $(ROOT)/cg/OrthogonalCom; $(JAVAC) -classpath ../.. CG_Main.java)
run_codegen:
	(cd $(ROOT)/cg/OrthogonalCom; \
		$(JAVA) -classpath ../.. cg.OrthogonalCom.CG_Main)

run_non_codegen:
	$(JAVA) -classpath $(ROOT) ptolemy.actor.gui.CompositeActorApplication -class ptolemy.domains.sdf.demo.OrthogonalCom.OrthogonalCom -iterations 500
depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk



