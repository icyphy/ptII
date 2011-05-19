# Makefile for Classes that use Jython, the 100% Java implementation of Tcl
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2009 The Regents of the University of California.
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

ME =		ptolemy/actor/gui/python

DIRS =

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(JYTHON_DIR)/jython.jar

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	python
PTCLASSJAR =	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	PythonShellEffigy.java \
	PythonShellTableau.java

# Files to be included in the jar file
OTHER_FILES_TO_BE_JARED = \
	configuration.xml \
	help.htm \
	init.py

EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'PythonTableau$$1.class' \
	'PythonTableau$$Factory.class' \
	'PythonTableau$$TclShellFrame.class' \
	'PythonShellEffigy$$ShellFactory.class' \
	'PythonShellTableau$$Factory.class' \
	'PythonShellTableau$$PythonShellFrame.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass

install: jclass jars

demo: $(JCLASS)
	$(ROOT)/bin/moml configuration.xml init.py

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
