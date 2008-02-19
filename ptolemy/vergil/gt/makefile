# Makefile for the ptolemy/vergil/gt package
#
# @Authors: Thomas Huining Feng, based on a file by Thomas M. Parks
#
# @Version: $Id$
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


ME =		ptolemy/vergil/gt

DIRS =		img

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE =	gt
PTDIST =	$(PTPACKAGE)$(PTVERSION)
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
	img/img.jar

PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	AbstractGTFrame.java \
	CellPanelEditor.java \
	GTFrame.java \
	GTFrameController.java \
	GTIngredientsEditor.java \
	GTTableau.java \
	MatchResultTableau.java \
	MatchResultViewer.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	'GTFrame$$1.class' \
	'GTFrame$$2.class' \
	'GTFrame$$BatchMatchAction$$ModelFileFilter.class' \
	'GTFrame$$BatchMatchAction$$MultipleViewController.class' \
	'GTFrame$$BatchMatchAction.class' \
	'GTFrame$$CellEditor.class' \
	'GTFrame$$FileComparator.class' \
	'GTFrame$$LayoutAction.class' \
	'GTFrame$$MatchAction.class' \
	'GTFrame$$MatchResultRecorder.class' \
	'GTFrame$$SingleMatchAction$$SingleViewController.class' \
	'GTFrame$$SingleMatchAction.class' \
	'GTFrame$$ViewController.class' \
	'GTFrameController$$1.class' \
	'GTFrameController$$2.class' \
	'GTFrameController$$ConfigureOperationsAction.class' \
	'GTFrameController$$GTActorGraphController$$EntityController.class' \
	'GTFrameController$$GTActorGraphController$$NewRelationAction.class' \
	'GTFrameController$$GTActorGraphController$$PortController.class' \
	'GTFrameController$$GTActorGraphController.class' \
	'GTFrameController$$GTActorGraphModel.class' \
	'GTFrameController$$GTFSMGraphController.class' \
	'GTFrameController$$GTFSMGraphModel.class' \
	'GTFrameController$$GTMenuActionFactory.class' \
	'GTIngredientsEditor$$1.class' \
	'GTIngredientsEditor$$2.class' \
	'GTIngredientsEditor$$3.class' \
	'GTIngredientsEditor$$4.class' \
	'GTIngredientsEditor$$ColorizedComboBox$$Editor.class' \
	'GTIngredientsEditor$$ColorizedComboBox.class' \
	'GTIngredientsEditor$$ColorizedTextField.class' \
	'GTIngredientsEditor$$ComboElement.class' \
	'GTIngredientsEditor$$Factory.class' \
	'GTIngredientsEditor$$HelpLabel.class' \
	'GTIngredientsEditor$$IngredientContentEditor.class' \
	'GTIngredientsEditor$$Row$$CheckBoxActionListener.class' \
	'GTIngredientsEditor$$Row.class' \
	'GTTableau$$Factory.class' \
	'MatchResultTableau$$Factory.class' \
	'MatchResultViewer$$1.class' \
	'MatchResultViewer$$CloseAction.class' \
	'MatchResultViewer$$FileSelectionStatus.class' \
	'MatchResultViewer$$MatchResultNodeController.class' \
	'MatchResultViewer$$MatchResultViewerController.class' \
	'MatchResultViewer$$NextAction.class' \
	'MatchResultViewer$$NextFileAction.class' \
	'MatchResultViewer$$PreviousAction.class' \
	'MatchResultViewer$$PreviousFileAction.class' \
	'MatchResultViewer$$TransformAction.class' \
	'MatchResultViewer$$TransformAllAction.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
