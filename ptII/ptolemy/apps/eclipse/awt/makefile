# Makefile for Eclipse AWT Test
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# @Copyright (c) 2008 The Regents of the University of California
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

ME =		ptolemy/apps/eclipse/awt

DIRS =		

# Root of the Java directory
ROOT =		../../../..

ECLIPSEDIRECTORY = 
CLASSPATH =	$(ROOT)$(PTBACKTRACK_ECLIPSE_DOC_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk

include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	awt
PTCLASSJAR = 	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	Snippet135.java

OTHER_FILES_TO_BE_JARED = 

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES =

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
	@echo "To see the main demo, run 'make SWTVergilApplication'"

install: jclass jars

Snippet135:
	"$(JAVA)" -classpath "$(CLASSPATH)" ptolemy.apps.eclipse.awt.Snippet135


SWTVergilApplication: SWTVergilApplication.class
	"$(JAVA)" -classpath "$(CLASSPATH)" ptolemy.apps.eclipse.awt.SWTVergilApplication

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk

