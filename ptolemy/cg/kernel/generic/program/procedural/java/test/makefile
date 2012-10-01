# Makefile for the CG actor tests
#
# @Authors: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2005-2012 The Regents of the University of California.
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
ME =		ptolemy/cg/kernel/generic/program/procedural/java/test

# Root of the Ptolemy II directory
ROOT =		../../../../../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTCOLT_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	testDefs.tcl

JSRCS = \
	ActorWithPortNameProblem.java \
	ActorWithPrivateParameter.java \
	CustomAdder.java \
	ObjectTokenSink.java \
	ObjectTokenSource.java \
	PortParameterActor.java \
	ReadParametersAcrossLink.java \
	ReadParametersInContainer.java \
	ExpressionActorToBeAutoCodeGenerated.java

# Non-graphical Java tests written in Tcl
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JSIMPLE_TESTS) to EXTRA_SRCS
JSIMPLE_TESTS = \
	AutoAdapter.tcl \
	JavaCodeGenerator.tcl \
        JavaTemplateParser.tcl

# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) \
	ActorWithPrivateParameterTest.xml \
	AllSDFDotTcl \
	findAllSDFTests

#$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	alljtests.tcl  junit \
                     auto

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'ReadParametersAcrossLink$$1.class' \
	'ReadParametersAcrossLink$$PopulationGroup.class'

JCLASS =	$(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple

test_java: jclass

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# If necessary, build the actors and domains
RAMP_CLASS = $(ROOT)/ptolemy/cg/adapter/generic/program/procedural/java/adapters/ptolemy/actor/lib/Ramp.class

$(RAMP_CLASS): 
	if [ ! -f $(RAMP_CLASS) ]; then \
	    echo "Building cg/adapter classes so tests work"; \
	    cd $(ROOT)/ptolemy/cg/adapter; $(MAKE) fast; \
        fi
TESTGENERATOR_CLASS = $(ROOT)/ptolemy/cg/kernel/generic/test/TestGenericCodeGenerator.class
$(TESTGENERATOR_CLASS):
	if [ ! -f $(TESTGENERATOR_CLASS) ]; then \
	    echo "Building cg/kernel/generic/test classes so tests work"; \
	    cd $(ROOT)/ptolemy/cg/kernel/generic/test; $(MAKE) fast; \
        fi

# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) $(RAMP_CLASS) \
	$(TESTGENERATOR_CLASS) alljtests.tcl test_auto test_java_cg
	$(JTCLSH) alljtests.tcl
	-(cd $(ROOT)/ptolemy/actor/lib/test; $(MAKE) cleanFileWriter cleanRunCompositeActor)

# 'make clean' removes $(KRUFT)
KRUFT = 

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
