# Makefile for the ptolemy/vergil/basic package
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
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


ME =		ptolemy/vergil/basic

DIRS =		img

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	basic
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		img/img.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	AbstractBasicGraphModel.java \
	BasicGraphController.java \
	BasicGraphFrame.java \
	EditorDropTarget.java \
	ExtendedGraphFrame.java \
	IconController.java \
	LocatableNodeController.java \
	LocatableNodeDragInteractor.java \
	NamedObjController.java \
	NamedObjNodeModel.java \
	NodeControllerFactory.java \
	ParameterizedNodeController.java \
	RunnableGraphController.java \
	WithIconGraphController.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'AbstractBasicGraphModel$$GraphChangeListener.class' \
	'BasicGraphController$$1.class' \
	'BasicGraphController$$NewPortAction.class' \
	'BasicGraphController$$SchematicContextMenuFactory.class' \
	'BasicGraphController$$2.class' \
	'BasicGraphFrame$$1.class' \
	'BasicGraphFrame$$CopyAction.class' \
	'BasicGraphFrame$$CutAction.class' \
	'BasicGraphFrame$$ExecuteSystemAction.class' \
	'BasicGraphFrame$$ImportLibraryAction.class' \
	'BasicGraphFrame$$LayoutAction.class' \
	'BasicGraphFrame$$PasteAction.class' \
	'BasicGraphFrame$$PtolemyLayout.class' \
	'BasicGraphFrame$$PtolemyLayoutTarget.class' \
	'BasicGraphFrame$$SaveInLibraryAction.class' \
	'BasicGraphFrame$$ZoomFitAction.class' \
	'BasicGraphFrame$$ZoomInAction.class' \
	'BasicGraphFrame$$ZoomOutAction.class' \
	'BasicGraphFrame$$ZoomResetAction.class' \
	'EditorDropTarget$$1.class' \
	'EditorDropTarget$$2.class' \
	'EditorDropTarget$$3.class' \
	'EditorDropTarget$$4.class' \
	'EditorDropTarget$$DTListener.class' \
	'IconController$$IconRenderer.class' \
	'NamedObjController$$GetDocumentationAction.class' \
	'ParameterizedNodeController$$1.class' \
	'RunnableGraphController$$PauseModelAction.class' \
	'RunnableGraphController$$RunModelAction.class' \
	'RunnableGraphController$$StopModelAction.class

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
