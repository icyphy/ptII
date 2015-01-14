# Makefile the Ptolemy II test suite that uses Tcl Blend and JUnit
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2010-2015 The Regents of the University of California.
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

# Root of the Java directory
ROOT =		../../../..

ME =		ptolemy/util/test/junit

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTJACL_JAR)$(CLASSPATHSEPARATOR)$(JUNIT_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	junit
PTCLASSJAR = 	$(PTPACKAGE).jar

# Java Sources, keep these alphabetical,
JSRCS = \
	AutoCGCKnownFailedTests.java \
	AutoCGCInlineTests.java \
	AutoCGCNoInlineTests.java \
	AutoCGCNoInlineArduinoTests.java \
	AutoCGCTests.java \
	AutoCGFMIMANoInlineTests.java \
	AutoCGFMIMAKnownFailedTests.java \
	AutoCGJavaKnownFailedTests.java \
	AutoCGJavaTests.java \
	AutoCGKnownFailedTests.java \
	AutoCGTests.java \
	AutoKnownFailedTests.java \
	Auto32Tests.java \
	AutoTests.java \
	JUnitAuto32Base.java \
	JUnitCGCArduinoTestBase.java \
	JUnitCGCTestBase.java \
	JUnitCGCNoInlineTestBase.java \
	JUnitCGFMIMATestBase.java \
	JUnitCGJavaTestBase.java \
	JUnitTclTest.java \
	JUnitTclTestBase.java \
	JUnitTclTestRun.java \
	ModelKnownFailedTests.java \
	ModelTests.java \
	TclTests.java \

TCL_SRCS =

# Shell scripts that should be made executable
SCRIPTS = \
	javachdir \
	updateJUnitCGJavaTest \
	updateJUnitTclTest

EXTRA_SRCS =	$(JAVA_SRCS) $(TCL_SRCS) $(SCRIPTS) \
	JUnitCGJavaTest.java.in \
	makefile.in


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = \
	JUnitTclTest.java.in \
	javachdir32.in

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	'AutoCGCTests$$1.class' \
	'AutoCGJavaTests$$1.class' \
	'AutoKnownFailedTests$$1.class' \
	'AutoTests$$1.class' \
	'ModelTests$$1.class' \
	'ModelTests$$2.class' \
	'TclTests$$1.class' \
	'TclTests$$2.class' \
	javachdir32


JCLASS = $(JSRCS:%.java=%.class)

# JUNIT_JAVA_ARGS is set in $PTII/mk/ptII.mk
tests:: $(EXTRA_SRCS) jclass test_java #test_jsimple
	(cd ..; CLASSPATH="$(PTII)$(CLASSPATHSEPARATOR)$(CLASSPATH)" "$(JAVA)" $(JUNIT_JAVA_ARGS) org.junit.runner.JUnitCore ptolemy.util.test.junit.JUnitTclTest)


# Do make sources so that we ensure that the subdirectories are checked out
all:  jclass sources $(MISC_SRCS)
	-chmod a+x $(SCRIPTS)

install: all

test_java: jclass

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# These tests do not require a graphics terminal, but do use Jacl and Java
#test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS)
#	$(JTCLSH) alljtests.tcl



# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
