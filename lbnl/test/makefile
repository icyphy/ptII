# Makefile for LBNL Simulator tests
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# @Copyright (c) 2009 The Regents of the University of California
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA  OR RESEARCH IN MOTION
# LIMITED BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL,
# OR CONSEQUENTIAL DAMAGES ARISING OUT OF THE USE OF THIS SOFTWARE AND
# ITS DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA OR
# RESEARCH IN MOTION LIMITED HAVE BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA AND RESEARCH IN MOTION LIMITED
# SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA  AND RESEARCH IN MOTION LIMITED
# HAVE NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
##########################################################################

ME =		lbnl/test

DIRS =

# Root of the Java directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS =


# Keep this list alphabetized.
JSRCS =

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

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) auto junit

# These files don't exist because testQuatre uses testDeux and testTrois
#	testQuatre.c
#	testQuatre.h

EXTRA_SRCS =	testDefs.tcl $(TCL_SRCS) $(JSRCS) #$(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

JCLASS = $(JSRCS:%.java=%.class)

KRUFT = *.dll *.dyllib socket.cfg utilSocket.log auto/simulation.log

tests:: $(EXTRA_SRCS) jclass all_bins test_auto_path #test_jsimple

test_java: jclass

all: all_bins

all_bins:
	@if [ "$(PTLBNL_CROOM_DIR)" = "CRoom" ]; then \
		echo "Running (cd ../demo/CRoom; $(MAKE)"; \
		(cd ../demo/CRoom; $(MAKE)); \
	else \
		echo "The PTLBNL_CROOM_DIR had the value $(PTLBNL_CROOM_DIR) which is not CRoom so we are not running make in ../demo/CRoom.  This will cause tests to fail.  Maybe rerun configure and check to see if libexpat is found?"; \
	fi

install:
	@echo "Nothing to be done for install in $(ME)"

# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# Cause lbnl/actor/lib/{Simulator,SystemCommand}.java to go headless
JTCLSHFLAGS=-Dptolemy.ptII.isHeadless=true

# These tests do not require a graphics terminal, but do use Jacl and Java
test_jsimple: $(EXTRA_SRCS) jclass $(KERNEL_TESTDEFS) alljtests.tcl test_auto_path
	$(JTCLSH) alljtests.tcl

# Set PATH, LD_LIBRARY_PATH and DYLD_LIBRARY_PATH
test_auto_path:
	PATH="`pwd`:`$(PTII)/bin/ptcygpath -u $(PTII)/lbnl/lib/util`:$(PTII)/lib:$(PATH)"; export PATH; \
	LD_LIBRARY_PATH="`pwd`:$(PTII)/lbnl/lib/util:$(PTII)/lib:$(LD_LIBRARY_PATH)"; export LD_LIBRARY_PATH; \
	DYLD_LIBRARY_PATH="`pwd`:$(PTII)/lbnl/lib/util:$(PTII)/lib:$(DYLD_LIBRARY_PATH)"; export DYLD_LIBRARY_PATH; \
	$(JTCLSH) $(ROOT)/util/testsuite/auto.tcl

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
