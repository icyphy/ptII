# Makefile for the Ptolemy with Single Window Interface
#
# @Authors: Christopher Hylands (Makefile Only)
#
# @Version: $Id$
#
# @Copyright (c) 2003 The Regents of the University of California.
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

ME =		ptolemy/configs/singleWindow

# There is no demo directory, put demos in the corresponding domain
# directory so that we can find them if we are looking at domains.
# An alternative would be to split the demo subdirectory into
# directories by domain
DIRS =		#demo

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	singleWindow
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS =

# HTML Files that are derived from other HTML files.
DERIVED_FILES =

OTHER_FILES_TO_BE_JARED = \
	$(DERIVED_FILES) \
	singleWindowConfiguration.xml \
	singleWindowWelcomeWindow.xml \
	singleWindowTableauFactory.xml


EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass $(DERIVED_FILES)
	@echo "nothing need be done for make all in this directory"

install: all jars
	@echo "nothing need be done for make install in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
