# Makefile for the Vergil toolbox support package
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2003 The Regents of the University of California.
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

ME =		ptolemy/vergil/toolbox

DIRS = 		test

# Root of the Java directory
ROOT =		../../..

# Classloadingservice needs /boot to find the BootstrapClassLoader
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	toolbox
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	AnnotationEditorFactory.java \
	AttributeBoundsManipulator.java \
	ConfigureAction.java \
	EditIconAction.java \
	FigureAction.java \
	FileEditorTableauFactory.java \
	GraphicElement.java \
	MenuActionFactory.java \
	MenuItemFactory.java \
	PortSite.java \
	PtolemyListCellRenderer.java \
	PtolemyMenuFactory.java \
	PtolemyTransferable.java \
	RemoveIconAction.java \
	SnapConstraint.java \
	TextEditorConfigureFactory.java \
	TextEditorFactory.java \
	TextEditorForStringAttributes.java \
	TextEditorTableauFactory.java \
	VisibleParameterEditorFactory.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'AnnotationEditorFactory$$AnnotationTextEditor.class' \
	'EditParameterStylesFactory$$1.class' \
	'EditParametersFactory$$1.class' \
	'FigureAction$$1.class' \
	'FigureAction$$SourceType.class' \
	'GraphPalette$$1.class' \
	'GraphPalette$$EditorDragGestureListener.class' \
	'GraphPalette$$LayoutListener.class' \
	'GraphPalette$$NodeTransferable.class' \
	'GraphPalette$$PaletteController$$NodeDnDInteractor.class' \
	'GraphPalette$$PaletteController.class' \
	'JDesignTree$$1.class' \
	'JDesignTree$$DesignTreeDragGestureListener.class' \
	'VisibleParameterEditorFactory$$1.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
