# Makefile for the Vergil/ptolemy package
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 1998-2001 The Regents of the University of California.
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

DIRS =		kernel fsm

# Root of the Java directory
ROOT =		../../../

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	ptolemy
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		fsm/fsm.jar \
		img/img.jar \
		kernel/kernel.jar

PTCLASSALLJAR = ptolemy.jar

JSRCS = \
	AbstractPtolemyGraphModel.java \
	AttributeNodeModel.java \
	CompositeEntityModel.java \
	EditorDropTarget.java \
	GraphFrame.java \
	LocatableNodeController.java \
	LocatableNodeDragInteractor.java \
	PtolemyNodeModel.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'AbstractPtolemyGraphModel$$1.class' \
	'AbstractPtolemyGraphModel$$GraphChangeListener.class' \
	'EditorDropTarget$$1.class' \
	'EditorDropTarget$$2.class' \
	'EditorDropTarget$$3.class' \
	'EditorDropTarget$$4.class' \
	'EditorDropTarget$$DTListener.class' \
	'GraphFrame$$1.class' \
	'GraphFrame$$DeletionListener.class' \
	'GraphFrame$$GetDocumentationAction.class' \
	'GraphFrame$$ImportLibraryAction.class' \
	'GraphFrame$$SaveInLibraryAction.class' \
	'Ptolemy.properties' \
	'GraphFrame$$CopyAction.class' \
	'GraphFrame$$CutAction.class' \
	'GraphFrame$$EditIconAction.class' \
	'GraphFrame$$ExecuteSystemAction.class' \
	'GraphFrame$$LayoutAction.class' \
	'GraphFrame$$PasteAction.class' \
	'GraphFrame$$PtolemyLayout.class' \
	'GraphFrame$$PtolemyLayoutTarget.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

depend:
	@echo "no dependencies in this directory"

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
