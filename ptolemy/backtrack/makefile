# Makefile for Ptolemy backtracking package
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# @Copyright (c) 2005-2009 The Regents of the University of California.
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
#
#
##########################################################################

ME =		ptolemy/backtrack

# PTBACKTRACK_ECLIPSE_DIR is set to eclipse by configure in $PTII/mk/ptII.mk
# if the eclipse jar files were found.
# We don't include test in DIRS because test runs backtracking which requires
# that jclass is run in the parent, but not yet in test.  Confusing . . .
DIRS =		$(PTBACKTRACK_ECLIPSE_DIR) automatic manual util xmlparser demo

# Root of the Java directory
ROOT =		../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	backtrack
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		automatic/automatic.jar \
		manual/manual.jar \
	        util/util.jar \
		xmlparser/xmlparser.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = Checkpoint.java \
        CheckpointState.java \
        Rollbackable.java

OTHER_FILES_TO_BE_JARED = \
        backtrack.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =	test automatic eclipse manual ui util xmlparser


# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	demo \
	doc

# .class files in test/ that are removed here because test is not in DIRS above.
# The reason this is important is if the user compiles with Java 1.6, and then
# runs "make clean", then all the .class files should be removed so that
# if the user runs with Java 1.5, then there are no problems.
KRUFT = \
	test/ArrayTest1Main.class \
	test/RandomTest1Main.class \
	test/Test1Main.class \
	test/Test2Main.class \
	test/array1/ArrayTest1.class \
	test/random1/RandomTest1.class \
	test/test1/Test1.class \
	test/test2/Test2.class

JCLASS = $(JSRCS:%.java=%.class)

all: jclass

install: all jars $(PTPACKAGE).jar

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
