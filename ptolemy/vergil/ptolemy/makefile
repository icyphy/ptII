# Makefile for the Vergil/ptolemy package
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2000 The Regents of the University of California.
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

ME =		ptolemy/vergil/ptolemy

DIRS =		fsm

# Root of the Java directory
ROOT =		../../../

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ptolemy.vergil.ptolemy
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		ptolemy.vergil.ptolemy.jar \
		fsm/fsm.jar

PTCLASSALLJAR = ptolemy.jar

JSRCS = \
	CompaanNotation.java \
	CompaanPerformance.java \
	DirectorService.java \
	FrameAttribute.java \
	PtolemyDocument.java \
	PtolemyNotation.java \
	PtolemyModule.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS) 

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'CompaanNotation$$1.class' \
	'CompaanNotation$$CompaanEntityRenderer.class' \
	'CompaanNotation$$CompaanListener.class' \
	'DirectorService$$1.class' \
	'DirectorService$$2.class' \
	'DirectorService$$3.class' \
	'Ptolemy.properties PtolemyDocument$$FSMFactory.class' \
	'PtolemyDocument$$Factory.class' \
	'PtolemyDocument$$IsDirtyListener.class' \
	'PtolemyModule$$1.class' \
	'PtolemyModule$$2.class' \
	'PtolemyModule$$3.class' \
	'PtolemyModule$$4.class' \
	'PtolemyModule$$5.class' \
	'PtolemyModule$$PaletteInitializer.class' \
	'PtolemyModule$$PtolemyLayout.class' \
	'PtolemyModule$$PtolemyLayoutTarget.class' \
	'PtolemyModule$$VergilExecutionListener.class' \
	'PtolemyModule$$editIconAction.class' \
	'PtolemyModule$$executeSystemAction.class' \
	'PtolemyModule$$layoutAction.class' \
	'PtolemyModule$$lookInsideAction.class' \
	'PtolemyModule$$newPortAction.class' \
	'PtolemyModule$$newRelationAction.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
