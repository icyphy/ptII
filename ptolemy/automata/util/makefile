# Makefile for the Java classes used to implement the automata package
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1997-1999 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSIPT OF CALIFORNIA BE LIABLE TO ANY PARPT
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSIPT OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILIPT OF
# SUCH DAMAGE.
#
# THE UNIVERSIPT OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILIPT AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSIPT OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY

ME =		ptolemy/automata/util

DIRS = 		test #doc

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Flags to pass to javadoc. (Override value in ptII.mk)
JDOCFLAGS = 	-author -version -public

# Used to build jar files
PTPACKAGE = 	util
PTPACKAGE_ROOTDIR = $(CLASSPATH)
PTPACKAGE_DIR = $(ME)
PTVERSION =	1.0
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSZIP =		$(PTPACKAGE_DIR)/$(PTPACKAGE).zip
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
        Variable.java \
        VariableList.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) doc

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR)

JCLASS = $(JSRCS:%.java=%.class)

# Needed for compiling under Cygwin on NT
LIB = dummylib

all: jclass
install: jclass javadocs $(PTCLASSJAR)
docs: javadocs

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk



