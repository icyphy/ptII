# Makefile for the JUnit Vergil basic tests
#
# @Authors: Christopher Brooks
#
# @Version: $Id$
#
# @Copyright (c) 2011 The Regents of the University of California.
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
ME =		ptserver/tests/junit

# Root of the Ptolemy II directory
ROOT =		../../..

# JUNIT_JAR is searched for by $PTII/configure and set in $PTII/mk/ptII.mk
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTSERVER_JARS)$(CLASSPATHSEPARATOR)$(JUNIT_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

JSRCS = \
	AllTests.java \
	FileDownloadTest.java \
	JUnitTclTest.java \
	RemoteModelTest.java \
	RESTGetHandlerTest.java \
	ServerTest.java \
	ServletTest.java \
	TokenParserTest.java \
	TypeParserTest.java

# Non-graphical Java tests written in Tcl
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JSIMPLE_TESTS) to EXTRA_SRCS
JSIMPLE_TESTS = \
	dummy.tcl

# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) #$(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	PtolemyLog.log \
	PtolemyServer.log \
	'RemoteModelTest$$1.class' \
	'RemoteModelTest$$2.class' \
	'RemoteModelTest$$3.class' \
	'RemoteModelTest$$4.class' \
	'RemoteModelTest$$5.class' \
	'RemoteModelTest$$6.class' \
	'ServletTest$$1.class'

JCLASS =	$(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

# JUNIT_JAVA_ARGS is set in $PTII/mk/ptII.mk
# Set ptolemy.ptII.isRunningNightlyBuild because MessageHandler will hang waiting for IO otherwise.
tests:: $(EXTRA_SRCS) jclass test_java ../../PtolemyServerConfig.properties #test_jsimple
	CLASSPATH="$(CLASSPATH)" "$(JAVA)" -Dptolemy.ptII.isRunningNightlyBuild=true $(JUNIT_JAVA_ARGS) org.junit.runner.JUnitCore ptserver.test.junit.AllTests

test_java: jclass

../../PtolemyServerConfig.properties: ../../PtolemyServerConfig.properties.default
	cp $^ $@

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl
	$(JTCLSH) alljtests.tcl

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
