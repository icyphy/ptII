# Makefile for gui/toolbox classes - Ptolemy II specific version of Diva
#
# @Version: $Id: makefile 47877 2008-01-24 00:35:12Z cxh $
# @Authors: Christopher Hylands (makefile only)
#
# @Copyright (c) 2003-2008 The Regents of the University of California.
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

ME =		diva/gui/toolbox

DIRS =

# Root of the Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	toolbox
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS = \
	FigureIcon.java \
	FocusMouseListener.java \
	JCanvasPalette.java \
	JCanvasPanner.java \
	JContextMenu.java \
	JPalette.java \
	JPanner.java \
	JPseudoFrame.java \
	JShadePane.java \
	JStatusBar.java \
	JTreePane.java \
	ListDataModel.java \
	MenuCreator.java \
	MenuFactory.java

EXTRA_SRCS =	$(JSRCS) \
	package.html

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	doc \
	$(PTCLASSJAR) \
	$(PTCLASSALLJAR) \
	'JCanvasPanner$$1.class' \
	'JCanvasPanner$$PanMouseListener.class' \
	'JPalette$$1.class' \
	'JPalette$$2.class' \
	'JPanner$$1.class' \
	'JPanner$$PanMouseListener.class' \
	'JPanner$$ScaleMouseListener.class' \
	'JPanner$$ScrollListener.class' \
	'JPseudoFrame$$1.class' \
	'JPseudoFrame$$2.class' \
	'JPseudoFrame$$3.class' \
	'JShadePane$$1.class' \
	'JShadePane$$2.class' \
	'JShadePane$$3.class' \
	'JShadePane$$4.class' \
	'JShadePane$$5.class' \
	'JShadePane$$6.class' \
	'JShadePane$$7.class' \
	'JShadePane$$Shade.class' \
	'JShadePane$$2$$1.class' \
	'JShadePane$$2$$2.class' \
	'JShadePane$$2$$3.class' \
	'JShadePane$$2$$4.class' \
	'JShadePane$$2$$5.class' \
	'JShadePane$$2$$6.class' \
	'JTreePane$$2.class' \
	'JTreePane$$1.class' \
	'JTreePane$$Entry.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
