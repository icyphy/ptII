# Makefile for the ptolemy/vergil/kernel/attributes package
#
# @Authors: Edward A. Lee
#
# @Version: $Id$
#
# @Copyright (c) 2001-2003 The Regents of the University of California.
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

ME =		ptolemy/vergil/kernel/attributes

DIRS =		

# Root of the Java directory
ROOT =		../../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	lib
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	EllipseAttribute.java \
	FilledShapeAttribute.java \
	ImageAttribute.java \
	LineAttribute.java \
	PolygonAttribute.java \
	RectangleAttribute.java \
	ShapeAttribute.java

OTHER_FILES_TO_BE_JARED = \
	decorative.xml


EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR)

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
