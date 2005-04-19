# Makefile for Ptolemy backtracking package
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# @Copyright (c) 2005 The Regents of the University of California
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
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
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

ME =		ptolemy/backtrack/test

DIRS =		array1 random1 test1 test2

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Tcl sources that are part of the testing infrastructure
TCL_SRCS = \
	testDefs.tcl

# Keep this list alphabetized.
JSRCS = ArrayTest1Main.java \
	RandomTest1Main.java \
	Test1Main.java \
	Test2Main.java

# Non-graphical Java tests written in Tcl
JSIMPLE_TESTS = \
	Backtrack.tcl

# Graphical Java tests that use Tcl.
# If there are no tests, we use a dummy file so that the script that builds
# alljtests.tcl works.  If you add a test, be sure to add
# $(JGRAPHICAL_TESTS) to EXTRA_SRCS
JGRAPHICAL_TESTS = \
	dummy.tcl

OTHER_FILES_TO_BE_JARED = 

EXTRA_SRCS =	$(TCL_SRCS) $(JSRCS) $(JSIMPLE_TESTS) #$(JGRAPHICAL_TESTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =	alljtests.tcl

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILptolemy.backtrack.ES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

tests:: $(EXTRA_SRCS) test_jsimple


# PtolemyII-wide test definitions.
KERNEL_TESTDEFS = $(ROOT)/util/testsuite/testDefs.tcl

# These tests do not require a graphics terminal, but do use Jacl and Java
# Don't include jclass here, Backtrack.tcl will run "make backtrack" for us.
test_jsimple: $(EXTRA_SRCS) $(KERNEL_TESTDEFS) alljtests.tcl
	$(JTCLSH) alljtests.tcl


# Backtrack.tcl runs "make backtrack" so that we can run the
# tests from inside Eclipse
backtrack: suball transform jclass

transform:
	# Transform the source and add prefix "ptolemy.backtrack.test"
	# to the package name.
	# The output root dir is $(ROOT).
	# The transformed code is in 
	# package ptolemy.backtrack.test.ptolemy.backtrack.test.test1.
	for x in $(DIRS); do \
	  echo "# transform: $$x"; \
	  rm -f ptolemy/backtrack/test/$$x/*.java; \
	  "$(JAVA)" \
	    -classpath "$(CLASSPATH)$(CLASSPATHSEPARATOR)$(PTBACKTRACK_JARS)$(AUXCLASSPATH)" \
	    ptolemy.backtrack.ast.Transformer \
	    -prefix ptolemy.backtrack.test \
	    -output "$(ROOT)" \
	    $$x/*.java; \
	done

run:
	# Finally, run the test program.
	for test in $(JSRCS); do \
	  echo "# run: $test"; \
	  "$(JAVA)" \
	    -classpath "$(CLASSPATH)" \
	    ptolemy.backtrack.test.`basename $$test .java`; \
	done

# Remove the ptolemy directory 
# It is generated during the testing, and should not be preserved.
KRUFT = ptolemy

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

