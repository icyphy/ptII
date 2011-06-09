# Makefile for Ptolemy run control panel classes
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2008 The Regents of the University of California.
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

ME =		ptolemy/actor/gui/run

# PTJACL_DIR is set to ptjacl by configure in $PTII/mk/ptII.mk if
# ptjacl.jar was found and the Ptolemy Tcl implementation is available

# $PTII/configure looks for Jython and sets PTJYTHON_DIR in
# $PTII/mk/ptII.mk if it was found.  Otherwise, PTJYTHON_DIR is empty.

DIRS =

# Root of the Java directory
ROOT =		../../../..

# PlotTableauFrame.java requires diva.jar
CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)$(CLASSPATHSEPARATOR)$(PTBSH_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	run
PTCLASSJAR =	$(PTPACKAGE).jar

# Keep this list alphabetized.
JSRCS = \
	CustomizableRunFrame.java \
	CustomizableRunPane.java \
	InterfaceTableau.java \
	LayoutTable.java \
	LayoutTableau.java \
	PaletteList.java \
	PtolemyFormEditor.java \
	RunLayoutFrame.java

# Files to be included in the jar file
OTHER_FILES_TO_BE_JARED = \
	ColumnDelete24.gif \
	ColumnInsertAfter24.gif \
	ColumnInsertBefore24.gif \
	editableLayoutConstraints.xml \
	Pack.gif \
	Remove24.gif \
	RowDelete24.gif \
	RowInsertAfter24.gif \
	RowInsertBefore24.gif
	
EXTRA_SRCS = $(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'CustomizableRunFrame$$CustomizeMenuListener$$1$$1.class' \
	'CustomizableRunFrame$$CustomizeMenuListener$$1.class' \
	'CustomizableRunFrame$$CustomizeMenuListener.class' \
	'CustomizableRunPane$$1.class' \
	'CustomizableRunPane$$2.class' \
	'CustomizableRunPane$$3.class' \
	'CustomizableRunPane$$4.class' \
	'InterfaceTableau$$Factory.class' \
	'PtolemyFormEditor$$1.class' \
	'PtolemyFormEditor$$2.class' \
	'PtolemyFormEditor$$3.class' \
	'PtolemyFormEditor$$4.class' \
	'PtolemyFormEditor$$5.class' \
	'PtolemyFormEditor$$6.class' \
	'PtolemyFormEditor$$7.class' \
	'PtolemyFormEditor$$ColSpanSpinnerModel.class' \
	'PtolemyFormEditor$$ComponentPaletteListModel.class' \
	'PtolemyFormEditor$$ComponentPaletteListRenderer.class' \
	'PtolemyFormEditor$$ConstraintTableCellRenderer.class' \
	'PtolemyFormEditor$$DeleteColumnAction.class' \
	'PtolemyFormEditor$$DeleteRowAction.class' \
	'PtolemyFormEditor$$GridTableModel.class' \
	'PtolemyFormEditor$$InsertColumnAfterAction.class' \
	'PtolemyFormEditor$$InsertColumnBeforeAction.class' \
	'PtolemyFormEditor$$InsertRowAfterAction.class' \
	'PtolemyFormEditor$$InsertRowBeforeAction.class' \
	'PtolemyFormEditor$$PackAction.class' \
	'PtolemyFormEditor$$RemoveComponentAction.class' \
	'PtolemyFormEditor$$RowSpanSpinnerModel.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass

install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
