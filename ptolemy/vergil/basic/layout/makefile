# Makefile for the Ptolemy II Layout tools
#
# @Authors: Thomas Huining Feng
#
# @Version: $Id$
#
# @Copyright (c) 2009-2012 The Regents of the University of California.
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

ME =            ptolemy/vergil/basic/layout

# Root of the ptII directory
ROOT =		../../../..


# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# configure sets PTKIELER_DIR to kieler if $PTII/lib/kieler.jar was found
DIRS = 		img $(PTKIELER_DIR)

# KIELER_JAR is set in $PTII/mk/ptII.mk to $(PTII)/lib/kieler.jar
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(KIELER_JAR)$(CLASSPATHSEPARATOR)$(GUAVA_JAR)

# Used to build zip files
PTPACKAGE = 	layout
PTCLASSJAR =
PTCLASSALLJAR = $(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	img/img.jar \
	kieler/kieler.jar

JSRCS = \
	LayoutConfiguration.java

OTHER_FILES_TO_BE_JARED = \
	layout.htm

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'LayoutConfiguration$$InteractionMode.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: all jars


# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
