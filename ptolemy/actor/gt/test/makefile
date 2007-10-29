# Makefile for the Java classes used to test the xslt actors
#
# @Authors: Thomas Huining Feng
#
# @Version: $Id$
#
# @Copyright (c) 2003-2005 The Regents of the University of California.
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
#                       PT_COPYRIGHT_VERSION_2
#                       COPYRIGHTENDKEY

# Location of this directory, relative to the Ptolemy II directory
ME =        ptolemy/actor/gt/test

# Root of the Ptolemy II directory
ROOT =      ../../../..

# XSLTransformer requires saxon
CLASSPATH = $(ROOT)

# Get configuration info
CONFIG =    $(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
    testDefs.tcl

JSRCS = \
    AllMatchingTestHelper.java

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
    MatchingTest1.tcl \
    MatchingTest2.tcl \
    XMLTest1.tcl \
    XMLTest2.tcl \
    XMLTest3.tcl \
    XMLTest4.tcl \
    XMLTest5.tcl \
    XMLTest6.tcl \
    XMLTest7.tcl

# Graphical Java tests.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
    dummy.tcl

EXTRA_SRCS = \
    host1_1.xml \
    host1_2.xml \
    host1_3.xml \
    host1_4.xml \
    host1_5.xml \
    host1_6.xml \
    host1_7.xml \
    host1_8.xml \
    host2_1.xml \
    host2_2.xml \
    host2_3.xml \
    host2_4.xml \
    host2_5.xml \
    host2_6.xml \
    host3_1.xml \
    host3_2.xml \
    host3_3.xml \
    host3_4.xml \
    host4_1.xml \
    host4_2.xml \
    host4_3.xml \
    host4_4.xml \
    host4_5.xml \
    host4_6.xml \
    host5_1.xml \
    host5_2.xml \
    host5_3.xml \
    host6_1.xml \
    host6_2.xml \
    host7_1.xml \
    host7_2.xml \
    MatchingTest1.tcl \
    MatchingTest2.tcl \
    rule1.xml \
    rule2.xml \
    rule3.xml \
    rule4.xml \
    rule5.xml \
    rule6.xml \
    XMLTest1.tcl \
    XMLTest2.tcl \
    XMLTest3.tcl \
    XMLTest4.tcl \
    XMLTest5.tcl \
    XMLTest6.tcl \
    XMLTest7.tcl \
    $(TCL_SRCS) $(JSRCS) #$(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =    alljtests.tcl

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
    'AllMatchingTestHelper$$1.class'

JCLASS =    $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple

test_java: jclass

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl test_auto
	$(JTCLSH) alljtests.tcl

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
