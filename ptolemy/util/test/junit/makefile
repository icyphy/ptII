# Makefile the Ptolemy II test suite that uses Tcl Blend and JUnit
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2010 The Regents of the University of California.
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

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTJACL_JAR)$(CLASSPATHSEPARATOR)$(JUNIT_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Java Sources, keep these alphabetical,
JSRCS = \
	AutoTests.java \
	PersonTest.java \
	AutoKnownFailedTests.java \
	JUnitTclTest.java \
	JUnitTclTestBase.java \
	TclTests.java

TCL_SRCS = \


# Shell scripts that should be made executable
SCRIPTS = \
	updateJUnitTclTest

EXTRA_SRCS =	$(JAVA_SRCS) $(TCL_SRCS) $(SCRIPTS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = \
	JUnitTclTest.java.in

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \

JCLASS = $(JSRCS:%.java=%.class)

# Do make sources so that we ensure that the subdirectories are checked out
all:  jclass sources $(MISC_SRCS)
	-chmod a+x $(SCRIPTS)

install: all


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
