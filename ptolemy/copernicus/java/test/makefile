# Makefile for java deep and shallow code generation tests 
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2001 The Regents of the University of California.
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
ME =		ptolemy/copernicus/java/test

# Root of the Ptolemy II directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(SOOT_CLASSES)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	testDefs.tcl \
	sootShallowCodeGeneration.tcl

JSRCS = \

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
	Shallow.tcl

# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS = \
	jad2java \
	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	alljtests.tcl

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS =	$(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple

test_java: jclass

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

PTJACL_JFLAG = -Xms180m -Xmx180m
# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl
	$(JTCLSH) alljtests.tcl

depend:
	@echo "no dependencies in this directory"
ptjacl:
	$(JTCLSH)

SOURCECLASS = $(ROOT)/ptolemy/domains/sdf/kernel/test/auto/test7.xml
SHALLOWTARGETPACKAGE = ptolemy.copernicus.java.test.cg
SHALLOWTARGETNAME = test7
SHALLOWTARGETCLASS = $(SHALLOWTARGETPACKAGE).$(SHALLOWTARGETNAME)

JVM_SIZE = -Xmx132m

SOOT_USER_ARGS =

# Useful rule for quickly testing
demo: compileShallowDemo runShallowDemo disassembleShallowDemo
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

# Run the output through the jad disassembler and then recompile it
# This can be very useful for debugging
disassembleShallowDemo:
	rm -f cg/$(SHALLOWTARGETNAME).jad
	jad -d cg cg/$(SHALLOWTARGETNAME).class
	sh ./jad2java cg/$(SHALLOWTARGETNAME).jad \
		> cg/$(SHALLOWTARGETNAME).java
	$(JAVAC) -classpath $(ROOT) cg/$(SHALLOWTARGETNAME).java
	CLASSPATH=$(ROOT) /opt/jdk1.3latest/bin/java \
		ptolemy.actor.gui.CompositeActorApplication \
		-class ptolemy.copernicus.java.test.cg.$(SHALLOWTARGETNAME)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk




