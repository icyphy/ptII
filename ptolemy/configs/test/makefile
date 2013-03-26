# Makefile for the Java classes used to test the Vergil configuration
#
# @Authors: Steve Neuendorffer
#
# @Version: $Id$
#
# @Copyright (c) 1997-2013 The Regents of the University of California.
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
ME =		ptolemy/actor/test

# Root of the Ptolemy II directory
ROOT =		../../..

# COMPLETE_CLASSPATH is defined in $PTII/mk/ptII.mk.in
CLASSPATH =	$(ROOT)$(COMPLETE_CLASSPATH)

# AUXCLASSPATH is used by jtclsh
AUXCLASSPATH =	$(CLASSPATH)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	cloneTest.tcl \
	configTest.tcl \
	testDefs.tcl

JSRCS = \
	ValidatingXMLParser.java

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
	allConfigs.tcl \
	allDemos.tcl \
	missingDemos.tcl \
	validDemos.tcl

# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

MODELS_TXT = ../doc/models.txt
EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) $(MODELS_TXT) $(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	alljtests.tcl junit

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'ValidatingXMLParser$$MoMLEntityResolver.class'

JCLASS =	$(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple

test_java: jclass

$(MODELS_TXT): ../doc/completeDemos.htm
	(cd ../doc; make models.txt)

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# These tests consume quite a bit of memory, so we increase the maximum
# stack size to 4000Mb
JAVAFLAGS = -Xmx4000m


# The allConfigs.tcl test now requires a graphical terminal
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl
	$(JTCLSH) alljtests.tcl



# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
