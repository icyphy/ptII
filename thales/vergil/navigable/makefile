# Makefile for Java vergil/navigable classes created by Thales
#
# @Version: $Id$
# @Author: Christopher Hylands
#
# This makefile has the following copyright:
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
##########################################################################

ME =		thales/vergil/navigable

DIRS =		img

# Root of Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	navigable
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		img/img.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	NavigableActorGraphFrame.java \
	NavigableActorGraphTableau.java \
	NavigationPTree.java \
	NavigationTreeModel.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES = 	img

OPTIONAL_FILES = \
	'NavigableActorGraphFrame$$DebugMenuListener.class' \
	'NavigableActorGraphFrame$$UpAction.class' \
	'NavigableActorGraphTableau$$Factory.class' \
	'NavigationPTree$$expandListener.class' \
	'NavigationPTree$$selectionListener.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: all jars 

# Include rules for directories that contain only subdirectories.
include $(ROOT)/mk/ptcommon.mk
