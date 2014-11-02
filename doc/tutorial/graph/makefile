# Makefile for testing the tutorial/graph java files
#
# @Authors: Christopher Brooks, based on a file by Thomas M. Parks
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

#

ME =		doc/tutorial/graph

# $PTII/configure looks for JUnit and sets PTJUNIT_DIR junit if it is found
DIRS =		$(PTJUNIT_DIR)

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	graph
PTCLASSJAR =

JSRCS = \
	ConnectivityReporter.java \
	HelloWorld.java \
	ShortestPathFinder.java

# Note that the TutorialApplet*.java files get copied to
# TutorialApplet.java and then made
EXTRA_SRCS =	$(JSRCS) \
	ConnectivityReporter.xml \
	HelloWorld.xml \
	ShortestPathFinder.xml \
	index.htm

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	junit \
	'ConnectivityReporter$$DoubleClickHandler.class' \
	'HelloWorld$$DoubleClickHandler.class' \
	'ShortestPathFinder$$Calculate.class' \
	$(PTCLASSJAR) \
	$(PTCLASSALLJAR)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

demo: all
	$(ROOT)/bin/vergil ConnectivityReporter.xml
	$(ROOT)/bin/vergil HelloWorld.xml
	$(ROOT)/bin/vergil ShortestPathFinder.xml

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
