# Ptolemy makefile for mlc swing layout
#
# @Version: $Id$
# @Author: Christopher Brooks
#
# @Copyright (c) 2007 The Regents of the University of California.
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

ME = 		org/mlc/swing/layout

DIRS =

# Root of Ptolemy II directory
ROOT =		../../../..

CLASSPATH = 	$(ROOT)$(CLASSPATHSEPARATOR)$(PTBSH_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	layout
PTCLASSJAR =	$(PTPACKAGE).jar

JSRCS = \
	BeanProperty.java \
	ButtonBarComponentBuilder.java \
	ComponentBuilder.java \
	ComponentDef.java \
	ContainerLayout.java \
	DefaultComponentBuilder.java \
	DnDTable.java \
	DndList.java \
	FormDebugPanel.java \
	FormEditor.java \
	JListComponentBuilder.java \
	JTableComponentBuilder.java \
	JToolBarComponentBuilder.java \
	JTreeComponentBuilder.java \
	LayoutConstraintsManager.java \
	LayoutFrame.java \
	MultiContainerFrame.java \
	NewComponentDialog.java \
	SeparatorComponentBuilder.java \
	TransferableWrapper.java \
	UserPrefs.java

OTHER_FILES_TO_BE_JARED = \
	Back16.gif \
	ColumnDelete24.gif \
	ColumnInsertAfter24.gif \
	ColumnInsertBefore24.gif \
	Down16.gif \
	Forward16.gif \
	New24.gif \
	Remove24.gif \
	RowDelete24.gif \
	RowInsertAfter24.gif \
	RowInsertBefore24.gif \
	Up16.gif \
	editableLayoutConstraints.xml \
	specificationLayoutConstraints.xml

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)

# Files or directories that are present, but that 'make checkjunk'
# should not complain about
# Don't include demo or DIRS here, or else 'make sources' will run 'make demo'
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'ContainerLayout$$LocationScore.class' \
	'FormEditor$$1.class' \
	'FormEditor$$2.class' \
	'FormEditor$$3.class' \
	'FormEditor$$4.class' \
	'FormEditor$$5.class' \
	'FormEditor$$6.class' \
	'FormEditor$$7.class' \
	'FormEditor$$8.class' \
	'FormEditor$$9.class' \
	'FormEditor$$ColSpanSpinnerModel.class' \
	'FormEditor$$ComponentListCellRenderer.class' \
	'FormEditor$$ComponentPaletteListModel.class' \
	'FormEditor$$ComponentPaletteListRenderer.class' \
	'FormEditor$$ComponentSelectionListModel.class' \
	'FormEditor$$ConstraintTableCellRenderer.class' \
	'FormEditor$$DeleteColumnAction.class' \
	'FormEditor$$DeleteRowAction.class' \
	'FormEditor$$GridTableModel.class' \
	'FormEditor$$InsertColumnAfterAction.class' \
	'FormEditor$$InsertColumnBeforeAction.class' \
	'FormEditor$$InsertRowAfterAction.class' \
	'FormEditor$$InsertRowBeforeAction.class' \
	'FormEditor$$NewComponentAction.class' \
	'FormEditor$$NewComponentDialog0$$1.class' \
	'FormEditor$$NewComponentDialog0$$2.class' \
	'FormEditor$$NewComponentDialog0$$3.class' \
	'FormEditor$$NewComponentDialog0$$PropertyTableModel.class' \
	'FormEditor$$NewComponentDialog0.class' \
	'FormEditor$$RemoveComponentAction.class' \
	'FormEditor$$RowSpanSpinnerModel.class' \
	'JTableComponentBuilder$$1.class' \
	'LayoutConstraintsManager$$1.class' \
	'LayoutFrame$$1.class' \
	'LayoutFrame$$2.class' \
	'LayoutFrame$$3.class' \
	'LayoutFrame$$4.class' \
	'LayoutFrame$$5.class' \
	'LayoutFrame$$CodeDialog$$1.class' \
	'LayoutFrame$$CodeDialog.class' \
	'LayoutFrame$$XmlFileFilter.class' \
	'NewComponentDialog$$1.class' \
	'NewComponentDialog$$2.class' \
	'NewComponentDialog$$3.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
