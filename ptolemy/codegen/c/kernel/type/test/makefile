# Makefile for Ptolemy Codegen C template test
# Based on standard Ptolemy makefiles
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# Makefile for the Java classes used to test the Ptolemy kernel
#
# @Authors: Christopher Hylands
#
# @Version: $Id$
#
# @Copyright (c) 1997-2009 The Regents of the University of California.
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

ME =		ptolemy/codegen/c/kernel/type/test


DIRS =		test

# Root of the Java directory
ROOT =		../../../../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	testDefs.tcl

CSRCS = \
	IntTest.c

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
	CUnit.tcl

# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	alljtests.tcl \
	IntTest.c \
	test

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	TestSerializable.data

JCLASS =	$(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

CUNIT_TESTS = \
	IntTest

KRUFT = $(CUNIT_TESTS)

all: $(CUNIT_TESTS)
	@echo "To run the tests, run 'make run' or 'make tests'"
install:

run: $(CUNIT_TESTS)
	@if [ "x$(CUNIT_TESTS)" != "x" ]; then \
		set $(CUNIT_TESTS); \
		for x do \
		    if [ -x $$x ] ; then \
			./$$x; \
		    fi; \
		done; \
	fi


CUNIT_INCLUDEDIR=/usr/local/include
CUNIT_LIB=-lcunit

sampleTest: sampleTest.c
	"$(PTCC)" -o $@ $< $(CUNIT_LIB)

IntTest: IntTest.c
	"$(PTCC)" -o $@ $< $(CUNIT_LIB)

tests:: $(EXTRA_SRCS) jclass test_java $(CUNIT_TESTS) test_jsimple

test_java: jclass

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl
# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl
	$(JTCLSH) alljtests.tcl


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
