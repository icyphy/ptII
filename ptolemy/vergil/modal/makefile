# Makefile for the ptolemy/vergil/modal package
#
# @Authors: Christopher Hylands, based on a file by Thomas M. Parks
#
# @Version: $Id$
#
# @Copyright (c) 2001-2009 The Regents of the University of California.
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


ME =		ptolemy/vergil/modal

DIRS =		\
		fmv \
		ia \
		modal

# Root of the Java directory
ROOT =		../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(DIVA_JAR)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	modal
PTCLASSJAR =

# Include the .class files from these jars in PTCLASSALLJAR
PTCLASSALLJARS = \
		ia/ia.jar \
		modal/modal.jar

# jars not included in modal.jar because they are not in ptiny
#		fmv/fmv.jar


PTCLASSALLJAR = $(PTPACKAGE).jar

JSRCS = \
	Arc.java \
	CaseGraphFrame.java \
	CaseGraphTableau.java \
	FSMGraphController.java \
	FSMGraphFrame.java \
	FSMGraphModel.java \
	FSMGraphTableau.java \
	FSMViewerGraphController.java \
	KielerLayoutArcConnector.java \
	StateController.java \
	StateIcon.java \
	TransitionController.java

EXTRA_SRCS =	$(JSRCS)

# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
MISC_FILES =	$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make realclean removes OPTIONAL_FILES
OPTIONAL_FILES = \
	$(PTCLASSJAR) \
	'CaseGraphFrame$$1.class' \
	'CaseGraphFrame$$AddCaseAction$$1.class' \
	'CaseGraphFrame$$AddCaseAction.class' \
	'CaseGraphFrame$$CaseGraphController.class' \
	'CaseGraphFrame$$RemoveCaseAction$$1.class' \
	'CaseGraphFrame$$RemoveCaseAction.class' \
	'CaseGraphTableau$$Factory.class' \
	'FSMGraphController$$LinkCreator.class' \
	'FSMGraphController$$NewStateAction.class' \
	'FSMGraphFrame$$DebugMenuListener.class' \
	'FSMGraphFrame$$FSMGraphPane.class' \
	'FSMGraphFrame$$LayoutAction.class' \
	'FSMGraphModel$$ArcModel$$1.class' \
	'FSMGraphModel$$ArcModel$$2.class' \
	'FSMGraphModel$$ArcModel$$3.class' \
	'FSMGraphModel$$ArcModel$$4.class' \
	'FSMGraphModel$$ArcModel$$5.class' \
	'FSMGraphModel$$ArcModel$$6.class' \
	'FSMGraphModel$$ArcModel.class' \
	'FSMGraphModel$$PortModel$$1.class' \
	'FSMGraphModel$$PortModel.class' \
	'FSMGraphModel$$StateModel$$1.class' \
	'FSMGraphModel$$StateModel.class' \
	'FSMGraphTableau$$Factory.class' \
	'StateController$$EditIconAction$$1.class' \
	'StateController$$EditIconAction.class' \
	'StateController$$LookInsideAction.class' \
	'StateController$$RemoveIconAction.class' \
	'StateController$$StateRenderer$$1.class' \
	'StateController$$StateRenderer.class' \
	'TransitionController$$LinkDropper.class' \
	'TransitionController$$LinkRenderer.class' \
	'TransitionController$$LinkTarget.class' \
	'TransitionController$$LookInsideAction.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass jars

# Get the rest of the rules
include $(ROOT)/mk/ptcommon.mk
