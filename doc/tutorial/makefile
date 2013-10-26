# Makefile for testing the tutorial java files
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2000-2009 The Regents of the University of California.
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
# To run the demos, run "make demo"
ME =		doc/tutorial

DIRS = 		domains graph gui

# Root of the Ptolemy II directory
ROOT =		../..

# These classes use ptolemy/actor/gui/PlotTableauFrame.java which requires
# diva.jar, so we add diva.jar to the CLASSPATH in case PlotTableauFrame.java
# has not yet been compiled.
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	tutorial
PTCLASSJAR =

JSRCS = \
	Precocious.java \
	TutorialApplet1.java \
	TutorialApplet2.java \
	TutorialApplet3.java

# Note that the TutorialApplet*.java files get copied to
# TutorialApplet.java and then made
EXTRA_SRCS =	$(JSRCS) \
	README.txt \
	TutorialApplet1.htm \
	TutorialApplet2.htm \
	TutorialApplet3.htm \
	TutorialApplet4.htm \
	index.htm

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	$(PTCLASSJAR) \
	$(PTCLASSALLJAR) \
	'Precocious$$1.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

demo: all
	appletviewer TutorialApplet1.htm
	appletviewer TutorialApplet2.htm
	appletviewer TutorialApplet3.htm
	appletviewer TutorialApplet4.htm

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
