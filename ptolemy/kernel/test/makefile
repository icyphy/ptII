# Makefile for the Java classes used to test the Ptolemy kernel
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1997 The Regents of the University of California.
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

# Note that the Java classes contained in this directory were written
# by Sun.  These classes came from tcljava, see license.terms for more info.

# Root of the Java directory
ROOT =		../../../..

ME =		java/pt/kernel

# Get configuration info
CONFIG =	$(ROOT)/mk/tycho.mk
include $(CONFIG)

CLASSPATH = ../../..

TCL_SRCS = \
	testDefs.tcl

JSRCS = \
#	RampApplet.java \
#	SDFPort.java \
#	SDFRamp.java \
#	SDFPlot.java \
#	SDFPrint.java\
#	SDFStar.java \
#	StatusArrayTest.java \
#	Sample.java \
#	SampleTarget.java \
#	SampleSDFExecutive.java

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
	NamedObj.tcl \
	NamedObjList.tcl

# Graphical Java tests that use Tcl.
# Since there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) \
	RampApplet.html ptjdriver #$(JGRAPHICAL_TESTS) 

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	#doc

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =	#codeDoc

JCLASS = $(JSRCS:%.java=%.class)

# Don't include all or install rules here, we want the user
# to run 'make tests' to run the tests.

tests:: $(EXTRA_SRCS) jclass test_java test_jsimple
	chmod a+x ptjdriver

test_java: jclass
	#$(JAVAHOME)/bin/java -classpath ../../..:$(JAVAHOME)/lib/classes.zip pt.kernel.test.StatusArrayTest

# These tests do not require a graphics terminal, but do use TclBlend and Java
test_jsimple: $(EXTRA_SRCS) alljtests.tcl
	CLASSPATH=$(CLASSPATH)$(AUXCLASSPATH) $(JTCLSH) alljtests.tcl

docs: javadocs

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/tycommon.mk



